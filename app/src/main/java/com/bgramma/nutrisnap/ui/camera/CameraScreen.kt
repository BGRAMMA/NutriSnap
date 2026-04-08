package com.bgramma.nutrisnap.ui.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CameraScreen(viewModel : CameraViewModel = viewModel()) {

    val context = LocalContext.current

    // observing
    val isGranted by viewModel.isCameraGranted
    val isCapturing by viewModel.isCapturing
    val aiResult by viewModel.aiResult

    // 화면 전환 애니메이션
    val alpha by animateFloatAsState(
        targetValue = if (isCapturing) 0.8f else 0f,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "BlinkAnimation"
    )

    // 카메라 컨트롤러
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            // 이미지 캡쳐(사진 촬영) 기능 활성화
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }

    // 권한 런쳐
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    // 진입 시 권한 요청
    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if ( isGranted ) {
            CameraPreview(controller = cameraController)

            if ( alpha > 0f ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = alpha))
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "음식을 화면 중앙에 맞춰주세요.",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }

            if ( aiResult != null ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = aiResult!!,
                        color = Color.White,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            FloatingActionButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                shape = CircleShape,
                containerColor = Color.White,
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "촬영")
            }
        } else {
            Text(
                text = "카메라 권한이 거부되었습니다.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}