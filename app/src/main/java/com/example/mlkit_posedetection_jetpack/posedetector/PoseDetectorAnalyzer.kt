package com.example.mlkit_posedetection_jetpack.posedetector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.pose.Pose
import com.example.mlkit_posedetection_jetpack.posedetector.graphic.GraphicOverlay
import com.example.mlkit_posedetection_jetpack.posedetector.mlkit.PoseDetectorProcessor
import com.example.mlkit_posedetection_jetpack.posedetector.utils.BitmapUtils


class PoseDetectorAnalyzer(
    private val context: Context,
    private val cameraProvider: ProcessCameraProvider?,
    private val graphicOverlay: GraphicOverlay,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val lensFacing: Int,
    private val videoCapture: VideoCapture<Recorder>?,
    private val onResults: (bitmap: Bitmap, detectedPose: Pose) -> Unit,

    ) {

    private var poseDetectorProcessor : PoseDetectorProcessor? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var needUpdateGraphicOverlayImageSourceInfo: Boolean = true
    private var cameraSelector :  CameraSelector? = null

    init {
        poseDetectorProcessor = PoseDetectorProcessor()
        bindAllUseCase()
    }

    private fun bindAllUseCase() {
        cameraProvider?.unbindAll()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = bindAnalysisUseCase()

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)

    }
    @OptIn(ExperimentalGetImage::class) private fun bindAnalysisUseCase(): ImageAnalysis? {
        if (poseDetectorProcessor == null) {
            poseDetectorProcessor = PoseDetectorProcessor()
        }
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
                        image = imageProxy,
                        onPoseDetected = { results->
                            val bitmap = BitmapUtils.getBitmap(imageProxy)
                            if (bitmap != null) {
                                if (results != null) {
                                    onResults(bitmap, results)
                                }
                            }
                        }
                    )
                } catch (e: MlKitException) {
                    Log.e("Camera", "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        return analysisUseCase
    }
}