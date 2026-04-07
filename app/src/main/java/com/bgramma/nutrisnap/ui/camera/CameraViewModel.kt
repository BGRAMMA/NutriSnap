package com.bgramma.nutrisnap.ui.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel : ViewModel() {

    // 카메라 권한 허용
    private val _isCameraGranted = mutableStateOf(false)
    val isCameraGranted: State<Boolean> = _isCameraGranted

    fun onPermissionResult(isGranted : Boolean) {
        _isCameraGranted.value = isGranted
    }

    // 촬영 중 상태 관리
    private val _isCapturing = mutableStateOf(false)
    val isCapturing: State<Boolean> = _isCapturing

    // 촬영된 파일 주소 저장
    private val _capturedFileUri = mutableStateOf<Uri?>(null)
    val capturedFileUri: State<Uri?> = _capturedFileUri

    // 사진 촬영
    fun takePhoto(
        controller: LifecycleCameraController,
        context: Context
    ) {
        _isCapturing.value = true

        val fileName     = "photo_${System.currentTimeMillis()}.jpg"
        val file         = File(context.cacheDir, fileName)
        val outputOption = ImageCapture.OutputFileOptions.Builder(file).build()

        controller.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        delay(150)

                        _isCapturing.value = false

                        val finalUri = outputFileResults.savedUri ?: Uri.fromFile(file)
                        _capturedFileUri.value = finalUri

                        Log.d("Camera", "파일 저장 성공 -> $finalUri")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    Log.d("Camera", "촬영 실패 : ${exception.message}", exception)
                }
            }
        )
    }
}