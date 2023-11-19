package com.example.mlkit_posedetection_jetpack

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mlkit_posedetection_jetpack.camera_usecase.CameraScreen
import com.example.mlkit_posedetection_jetpack.camera_usecase.CameraViewModel
import com.example.mlkit_posedetection_jetpack.ui.theme.MLKitPoseDetectionJetpackTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitPoseDetectionJetpackTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = viewModel<CameraViewModel>()
                    // A surface container using the 'background' color from the theme
                    CameraScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}