package com.example.mlkit_posedetection_jetpack.posedetector.graphic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/** Draw the detected pose in preview.  */
class PoseGraphic(
    overlay: GraphicOverlay,
    private val pose: Pose
) : GraphicOverlay.Graphic(overlay) {
    private val leftBrush: Brush
    private val rightBrush: Brush
    private val whiteBrush: Brush
    private var strokeWidth = 10f

    init {

        whiteBrush = SolidColor(Color.White)
        strokeWidth = STROKE_WIDTH
        leftBrush = SolidColor(Color.Green)
        rightBrush = SolidColor(Color.Yellow)
    }

    override fun draw(canvas: DrawScope) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
        val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
        val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
        val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
        val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
        val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
        val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
        val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
        val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
        val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

        // Face
        drawLine(canvas, nose, leftEyeInner, whiteBrush)
        drawLine(canvas, leftEyeInner, leftEye, whiteBrush)
        drawLine(canvas, leftEye, leftEyeOuter, whiteBrush)
        drawLine(canvas, leftEyeOuter, leftEar, whiteBrush)
        drawLine(canvas, nose, rightEyeInner, whiteBrush)
        drawLine(canvas, rightEyeInner, rightEye, whiteBrush)
        drawLine(canvas, rightEye, rightEyeOuter, whiteBrush)
        drawLine(canvas, rightEyeOuter, rightEar, whiteBrush)
        drawLine(canvas, leftMouth, rightMouth, whiteBrush)

        drawLine(canvas, leftShoulder, rightShoulder, whiteBrush)
        drawLine(canvas, leftHip, rightHip, whiteBrush)

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftBrush)
        drawLine(canvas, leftElbow, leftWrist, leftBrush)
        drawLine(canvas, leftShoulder, leftHip, leftBrush)
        drawLine(canvas, leftHip, leftKnee, leftBrush)
        drawLine(canvas, leftKnee, leftAnkle, leftBrush)
        drawLine(canvas, leftWrist, leftThumb, leftBrush)
        drawLine(canvas, leftWrist, leftPinky, leftBrush)
        drawLine(canvas, leftWrist, leftIndex, leftBrush)
        drawLine(canvas, leftIndex, leftPinky, leftBrush)
        drawLine(canvas, leftAnkle, leftHeel, leftBrush)
        drawLine(canvas, leftHeel, leftFootIndex, leftBrush)

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightBrush)
        drawLine(canvas, rightElbow, rightWrist, rightBrush)
        drawLine(canvas, rightShoulder, rightHip, rightBrush)
        drawLine(canvas, rightHip, rightKnee, rightBrush)
        drawLine(canvas, rightKnee, rightAnkle, rightBrush)
        drawLine(canvas, rightWrist, rightThumb, rightBrush)
        drawLine(canvas, rightWrist, rightPinky, rightBrush)
        drawLine(canvas, rightWrist, rightIndex, rightBrush)
        drawLine(canvas, rightIndex, rightPinky, rightBrush)
        drawLine(canvas, rightAnkle, rightHeel, rightBrush)
        drawLine(canvas, rightHeel, rightFootIndex, rightBrush)

        // Draw all the points
        for (landmark in landmarks) {
            drawPoint(canvas, landmark, whiteBrush)
        }

        // Draw inFrameLikelihood for all points
//        if (showInFrameLikelihood) {
//            for (landmark in landmarks) {
//                canvas.drawText(
//                    String.format(Locale.US, "%.2f", landmark.inFrameLikelihood),
//                    translateX(landmark.position.x),
//                    translateY(landmark.position.y),
//                    whiteBrush
//                )
//            }
//        }
    }

    private fun drawPoint(canvas: DrawScope, landmark: PoseLandmark, paint: Brush) {
        val point = landmark.position3D
        // maybeUpdateBrushColor(Brush, canvas, point.z)
//        canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, Brush)
        canvas.drawCircle(brush = paint,radius = DOT_RADIUS, center = Offset(translateX(point.x), translateY(point.y)))
    }

    private fun drawLine(
        canvas: DrawScope,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Brush,
    ) {
        val start = startLandmark!!.position3D
        val end = endLandmark!!.position3D

        // Gets average z for the current body line
        val avgZInImagePixel = (start.z + end.z) / 2
        //maybeUpdateBrushColor(Brush, canvas, avgZInImagePixel)
        canvas.drawLine(
            brush = paint,
            start =  Offset(translateX(start.x), translateY(start.y)),
            end = Offset(translateX(end.x), translateY(end.y)),
            strokeWidth = strokeWidth
        )
    }

    companion object {
        private const val DOT_RADIUS = 8.0f
        private const val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
        private const val STROKE_WIDTH = 8.0f
        private const val POSE_CLASSIFICATION_TEXT_SIZE = 60.0f
    }
}
