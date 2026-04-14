package com.bgramma.nutrisnap.ui.camera

import android.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
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

            // 미리보기
            CameraPreview(controller = cameraController)

            // 촬영 이펙트
            if ( alpha > 0f ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = alpha))
                )
            }

            // 상단 안내 문구
            Surface(
                modifier = Modifier.fillMaxSize().align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "음식을 화면 중앙에 맞춰주세요.",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            CameraResultView(
                aiResult,
                modifier = Modifier.align(Alignment.Center)
            )

            CameraCaptureButton(
                { viewModel.takePhoto(cameraController, context) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

        } else {
            Text(
                text = "카메라 권한이 거부되었습니다.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// 분석 결과
@Composable
fun CameraResultView(
    aiResult : String?,
    modifier : Modifier = Modifier
) {
    if ( aiResult != null ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                    ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = aiResult,
                color = Color.White,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// 촬영 버튼
@Composable
fun CameraCaptureButton(onClick : () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(bottom = 32.dp),
        shape = CircleShape,
        containerColor = Color.White
    ) {
        Icon(Icons.Default.PhotoCamera, contentDescription = "촬영")
    }
}