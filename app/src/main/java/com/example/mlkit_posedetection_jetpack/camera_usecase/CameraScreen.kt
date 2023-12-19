package com.example.mlkit_posedetection_jetpack.camera_usecase

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mlkit_posedetection_jetpack.R
import com.example.mlkit_posedetection_jetpack.posedetector.graphic.CameraImageGraphic
import com.example.mlkit_posedetection_jetpack.posedetector.graphic.GraphicOverlay
import com.example.mlkit_posedetection_jetpack.posedetector.graphic.PoseGraphic
import com.google.mlkit.vision.pose.Pose

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("UnsafeOptInUsageError", "RememberReturnType")
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val graphicOverlay = remember { GraphicOverlay() }
    val poseResult = remember { mutableStateOf<Pose?>(null) }
    val bitmapImage = remember { mutableStateOf<Bitmap?>(null) }
    val cameraSelector: MutableState<Int> = remember {
        mutableStateOf(CameraSelector.LENS_FACING_BACK)
    }

    val cameraViewModel: CameraViewModel = remember {
        CameraViewModel(
            graphicOverlay = graphicOverlay,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector.value,
            onResults = { bitmap, pose ->
                bitmapImage.value?.recycle()
                bitmapImage.value = null
                bitmapImage.value = bitmap
                poseResult.value = pose
            }
        )
    }
    val bindAnalysisData = remember { cameraViewModel.bindAllUseCase(context = context, previewView = previewView) }
    Scaffold{ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned {
//                    Log.d("BoxSize","${it.size.width} ${it.size.height}")
                }
        ) {
            CameraPreview(
                previewView = previewView,
                modifier = Modifier
                    .fillMaxSize()
            )

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (bitmapImage.value != null && poseResult.value!=null) {
                    graphicOverlay.updateGraphicOverlay(
                        width = size.width,
                        height = size.height,
                        isFlipped = cameraSelector.value == CameraSelector.LENS_FACING_FRONT
                    )
                    graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmapImage.value!!))
                    graphicOverlay.add(PoseGraphic(graphicOverlay, poseResult.value!!))
                    graphicOverlay.onDraw(this)
                    graphicOverlay.clear()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        cameraSelector.value = if (cameraSelector.value == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                        Log.d("CameraSelector", "CameraSelector: ${cameraSelector.value}")
                        cameraViewModel.changeCameraFacing(cameraSelector.value, previewView = previewView, context = context)
                    },
                    modifier = Modifier
                        .offset(16.dp, 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera_switch),
                        contentDescription = "Camera Flip",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }
        }
    }

}
