package com.example.revlens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.example.revlens.di.wasmModule
import com.example.revlens.ui.navigation.RevLensNavGraph
import com.example.revlens.ui.theme.RevLensTheme
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(wasmModule)
    }
    ComposeViewport(document.getElementById("root")!!) {
        RevLensTheme {
            RevLensNavGraph(modifier = Modifier.fillMaxSize())
        }
    }
}
