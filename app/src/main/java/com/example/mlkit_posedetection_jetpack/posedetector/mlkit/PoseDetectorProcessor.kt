package com.example.mlkit_posedetection_jetpack.posedetector.mlkit

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseDetectorProcessor: VisionImageProcessor {

    private val detector: PoseDetector

    init {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        detector = PoseDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun processImageProxy(image: ImageProxy?, onPoseDetected: (Pose?) -> Unit) {
        val inputImage = InputImage.fromMediaImage(image?.image!!, image.imageInfo.rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                onPoseDetected(pose)
            }
            .addOnCompleteListener{
                image.close()
            }
    }

    override fun stop() {
        detector.close()
    }
}