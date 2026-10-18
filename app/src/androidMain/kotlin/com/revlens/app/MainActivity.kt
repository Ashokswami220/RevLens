package com.revlens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.revlens.app.ui.navigation.RevLensNavGraph
import com.revlens.app.ui.theme.RevLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevLensTheme {
                RevLensNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}