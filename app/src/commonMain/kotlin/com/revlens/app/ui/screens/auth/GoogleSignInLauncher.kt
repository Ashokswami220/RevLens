package com.revlens.app.ui.screens.auth

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGoogleSignInLauncher(
    onTokenReceived: (idToken: String?, email: String?, displayName: String?) -> Unit
): () -> Unit
