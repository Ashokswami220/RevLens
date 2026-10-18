package com.revlens.app.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGoogleSignInLauncher(
    onTokenReceived: (idToken: String?, email: String?, displayName: String?) -> Unit
): () -> Unit {
    return remember(onTokenReceived) {
        {
            onTokenReceived(null, null, null)
        }
    }
}
