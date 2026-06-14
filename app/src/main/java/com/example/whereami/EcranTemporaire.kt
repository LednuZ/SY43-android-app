package com.example.whereami



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.whereami.AdvancedParametersScreen

class EcranTemporaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AdvancedParametersScreen()
        }
    }
}