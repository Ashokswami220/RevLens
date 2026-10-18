package com.revlens.app.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private const val WEB_CLIENT_ID = "512497569694-mev6evlr1e702fdqdmn2po203up65a84.apps.googleusercontent.com"

@Composable
actual fun rememberGoogleSignInLauncher(
    onTokenReceived: (idToken: String?, email: String?, displayName: String?) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val googleSignInClient = remember(context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                onTokenReceived(account.idToken, account.email, account.displayName)
            } catch (e: Exception) {
                onTokenReceived(null, null, null)
            }
        } else {
            onTokenReceived(null, null, null)
        }
    }

    return remember(launcher, googleSignInClient) {
        {
            launcher.launch(googleSignInClient.signInIntent)
        }
    }
}
