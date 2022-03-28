package com.jetpack.mlkitfacedetection

import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.jetpack.mlkitfacedetection.ui.theme.MLKitFaceDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitFaceDetectionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MLKitFaceDetection()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MLKitFaceDetection() {
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
                                          LaunchedEffect(Unit) {
                                              cameraPermissionState.launchPermissionRequest()
                                          }
                                      },
        permissionNotAvailableContent = {
            Column {
                Text(text = "Camera Permission Denied.")
            }
        }) {
        FaceRecognitionScreenContent()
    }
}

@Composable
fun FaceRecognitionScreenContent() {
    val lifeCycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)

                    cameraProviderFuture.addListener( {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(previewView.width, previewView.height))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setImageQueueDepth(10)
                            .build()
                            .apply {
                                setAnalyzer(executor, FaceAnalyzer())
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifeCycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    }, executor)
                    previewView
                }
            )
        }
    }
}


























