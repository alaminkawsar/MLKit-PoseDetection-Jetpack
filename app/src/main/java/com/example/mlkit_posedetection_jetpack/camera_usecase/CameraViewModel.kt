package com.example.mlkit_posedetection_jetpack.camera_usecase

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mlkit_posedetection_jetpack.posedetector.graphic.GraphicOverlay
import com.example.mlkit_posedetection_jetpack.posedetector.mlkit.PoseDetectorProcessor
import com.example.mlkit_posedetection_jetpack.posedetector.utils.BitmapUtils
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException

/** View model for interacting with CameraX. */
class CameraViewModel(
    private val graphicOverlay: GraphicOverlay,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraSelector: Int,
    private val onResults: (bitmap: android.graphics.Bitmap, Pose) -> Unit

) : ViewModel() {
    private val poseDetectorProcessor by lazy {
        PoseDetectorProcessor()
    }
    private val TAG = "CameraXViewModel"
    private var cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
    private var recording: Recording? = null
    private var needUpdateGraphicOverlayImageSourceInfo: Boolean = true
    private var bitmap: android.graphics.Bitmap? = null
    private var lensFacing: Int = cameraSelector
    private var imageAnalysis: ImageAnalysis? = null
    private var videoCapture: VideoCapture<Recorder>? = null


    /**
     * Create an instance which interacts with the camera service via the given application context.
     */
    private fun getProcessCameraProvider(context: Context): MutableLiveData<ProcessCameraProvider> {
        if (cameraProviderLiveData.value == null) {
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            viewModelScope.launch {
                try {
                    cameraProviderLiveData.value = cameraProviderFuture.get()
                    Log.d("CameraProvider", "hi")
                } catch (e: ExecutionException) {
                    // Handle any errors (including cancellation) here.
                    Log.e(TAG, "Unhandled exception", e)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "InterruptedException", e)
                }
            }
        }
        Log.d("CameraProviderX", "${cameraProviderLiveData.value}")
        return cameraProviderLiveData
    }

    fun changeCameraFacing(lensFacing: Int,context: Context, previewView: PreviewView) {
        this.lensFacing = lensFacing
        this.videoCapture = null
        bindAllUseCase(context = context, previewView = previewView)
    }

    fun bindAllUseCase(
        context: Context,
        previewView: PreviewView,
    ) {
        // check camera permission
        requestAllPermission(context)
        if (cameraProviderLiveData.value == null) {
            cameraProviderLiveData = getProcessCameraProvider(context)
        }
        cameraProviderLiveData.value?.unbindAll()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        if (imageAnalysis == null) {
            imageAnalysis = bindAnalysisUseCase(context)
        }
        //Log.d("CameraProvider","videoCapture")
        if (videoCapture == null) {
            videoCapture = createVideoCaptureUseCase(context)
        }

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // bind life cycle without video capture
        cameraProviderLiveData.value?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun bindAnalysisUseCase(context: Context): ImageAnalysis {
        val analysisUseCase = ImageAnalysis.Builder().build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                    } else {
                        graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    poseDetectorProcessor!!.processImageProxy(
                        image = imageProxy
                    ) { results ->
                        bitmap = BitmapUtils.getBitmap(imageProxy)
                        if (bitmap != null) {
                            if (results != null) {
                                onResults(bitmap!!, results)
                            }
                        }
                        bitmap?.recycle()
                        bitmap = null
                    }
                } catch (e: MlKitException) {
                    Log.e("Camera", "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        return analysisUseCase
    }

    private fun createVideoCaptureUseCase(context: Context): VideoCapture<Recorder> {
        val qualitySelector = QualitySelector.from(
            Quality.LOWEST,
            FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD)
        )
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .build()

        return VideoCapture.withOutput(recorder)
    }

    @SuppressLint("MissingPermission")
    fun startRecordingVideo(
        context: Context,
        videoCapture: VideoCapture<Recorder>,
        file_name: String,
        recordingStatus: (isCompleted: Boolean)-> Unit
    ) {
        if (recording != null) {
            recording?.stop()
            recording = null
            return
        }
        val audioEnabled = false
        val videoFile = File(context.filesDir, "$file_name.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .apply { if (audioEnabled) withAudioEnabled() }
            .start(ContextCompat.getMainExecutor(context)) { event->

                when(event) {
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            recording?.close()
                            recording = null
                            Toast.makeText(
                                context,
                                "Video capture failed",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val uri = event.outputResults.outputUri
                            if (uri != Uri.EMPTY) {
                                val uriEncoded = URLEncoder.encode(
                                    uri.toString(),
                                    StandardCharsets.UTF_8.toString()
                                )
                            }
                            Toast.makeText(
                                context,
                                "Video capture Succeeded",
                                Toast.LENGTH_LONG
                            ).show()
                            recordingStatus(true)
                        }
                    }
                    is VideoRecordEvent.Start -> {
                        Toast.makeText(
                            context,
                            "Video recording Started...",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is VideoRecordEvent.Pause -> {}

                    is VideoRecordEvent.Resume -> {}
                }
            }
    }

    fun requestAllPermission(context: Context) {
        if (!hasRequiredPermissions(context)) {
            Log.d("Permission1","permission check")
            ActivityCompat.requestPermissions(
                context as Activity, CAMERAX_PERMISSION,0
            )
        }
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        return CAMERAX_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        val CAMERAX_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

}
