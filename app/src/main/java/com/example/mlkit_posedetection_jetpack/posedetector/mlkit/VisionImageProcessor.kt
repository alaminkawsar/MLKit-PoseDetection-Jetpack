package com.example.mlkit_posedetection_jetpack.posedetector.mlkit

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose

interface VisionImageProcessor {

    fun processImageProxy(image: ImageProxy?, onPoseDetected:(Pose?)->Unit)

    fun stop()
}