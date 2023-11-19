package com.example.mlkit_posedetection_jetpack.camera_usecase

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException

/** View model for interacting with CameraX. */
class CameraViewModel : ViewModel() {

    private val TAG = "CameraXViewModel"
    private val cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
    private var recording: Recording? = null
    private var isRecordingCompleted = false

    /**
     * Create an instance which interacts with the camera service via the given application context.
     */
    fun getProcessCameraProvider(context: Context): MutableLiveData<ProcessCameraProvider> {
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
    fun createVideoCaptureUseCase(context: Context): VideoCapture<Recorder> {
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
