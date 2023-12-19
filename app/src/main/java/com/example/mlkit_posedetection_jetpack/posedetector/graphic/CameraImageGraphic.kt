package com.example.mlkit_posedetection_jetpack.posedetector.graphic

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay, private val bitmap: Bitmap) :
    GraphicOverlay.Graphic(overlay) {
    override fun draw(canvas: DrawScope) {
        Log.d("CameraImageGraphic","${bitmap.width} ${bitmap.height}")
        canvas.drawImage(bitmap.asImageBitmap())
    }
}