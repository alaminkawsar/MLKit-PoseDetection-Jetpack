package com.example.mlkit_posedetection_jetpack.posedetector.graphic

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay, private val bitmap: Bitmap) :
    GraphicOverlay.Graphic(overlay) {
    override fun draw(canvas: DrawScope) {
        // canvas.drawBitmap(bitmap, getTransformationMatrix(), null)
        val bitmapImage = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, getTransformationMatrix(), false
        )
         canvas.drawImage(bitmapImage.asImageBitmap())
    }
}