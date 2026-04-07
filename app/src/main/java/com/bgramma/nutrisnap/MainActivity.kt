package com.bgramma.nutrisnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bgramma.nutrisnap.ui.camera.CameraScreen
import com.bgramma.nutrisnap.ui.theme.NutriSnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriSnapTheme() {
                CameraScreen()
            }
        }
    }
}
