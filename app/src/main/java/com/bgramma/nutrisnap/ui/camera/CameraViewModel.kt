package com.bgramma.nutrisnap.ui.camera

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgramma.nutrisnap.BuildConfig
import com.bgramma.nutrisnap.data.FoodEntry
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat

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


    // gemini 모델 설정
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // gemini 결과 상태 관리
    private val _aiResult = mutableStateOf<String?>(null)
    val aiResult : State<String?> = _aiResult

    private val _foodList = mutableStateListOf<FoodEntry>()
    val foodList: List<FoodEntry> = _foodList

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

                        analyzeImage(context, finalUri)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    Log.d("Camera", "촬영 실패 : ${exception.message}", exception)
                }
            }
        )
    }

    fun analyzeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _aiResult.value = "분석 중..."

                val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text("""이 식단 사진에 있는 음식 이름과 칼로리를 알려줘. 다른 설명은 하지마.
                            {
                                "foodName": "음식 이름",
                                "calories": "숫자만(kcal 제외)",
                                "confidence": "분석 신뢰도(0.0~1.0)"
                            }
                            한국어로 답해줘.""".trimIndent())
                    }
                )

                val rawText = response.text ?: ""

                val jsonString = rawText
                    .replace("```json", "")
                    .replace("```","")
                    .trim()

                try {
                    val jsonObject = org.json.JSONObject(jsonString)
                    val foodName = jsonObject.getString("foodName")
                    val calories = jsonObject.getString("calories")
                    val confidence = jsonObject.getString("confidence").toDouble()

                    if ( confidence < 0.5 ) {
                        _aiResult.value = "음식 인식 결과가 불확실합니다. 다시 촬영해 주세요.\n(신뢰도 : ${(confidence * 100).toInt()}%)"
                        delay(3000)
                        _aiResult.value = null
                        return@launch
                    }

                    if ( foodName.contains("알 수 없음") || foodName.isEmpty() ) {
                        _aiResult.value = "사진에서 음식을 찾을 수 없습니다."
                        delay(3000)
                        _aiResult.value = null
                        return@launch
                    }

                    val currentTime = SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    val newEntry = FoodEntry(foodName, calories, currentTime)

                    _foodList.add(0, newEntry)

                    _aiResult.value = """
                        음식 : $foodName
                        칼로리 : ${calories}kcal
                        정확도 : ${(confidence * 100).toInt()}%
                    """.trimIndent()
                } catch (e : Exception) {
                    _aiResult.value = "데이터 해석 실패 : ${e.message}\n원문 : $rawText"
                }

            } catch (e: Exception) {
                _aiResult.value = "분석 실패: ${e.message}"
            }
        }
    }
}