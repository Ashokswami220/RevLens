package com.revlens.app.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private const val WEB_CLIENT_ID =
    "512497569694-mev6evlr1e702fdqdmn2po203up65a84.apps.googleusercontent.com"

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

    var isLaunching by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLaunching = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                onTokenReceived(account.idToken, account.email, account.displayName)
            } catch (e: ApiException) {
                android.util.Log.e(
                    "RevLensAuth", "Google sign-in failed with status code: ${e.statusCode}"
                )
                if (e.statusCode == 10) {
                    android.util.Log.e(
                        "RevLensAuth",
                        "DEVELOPER_ERROR (10): Register your SHA-1 fingerprint in Firebase Console under com.revlens.app"
                    )
                }
                onTokenReceived(null, null, null)
            } catch (e: Exception) {
                android.util.Log.e("RevLensAuth", "Google sign-in unexpected error: ${e.message}")
                onTokenReceived(null, null, null)
            }
        } else {
            android.util.Log.w(
                "RevLensAuth",
                "Google sign-in activity result was not OK (resultCode=${result.resultCode})"
            )
            onTokenReceived(null, null, null)
        }
    }

    return remember(launcher, googleSignInClient) {
        {
            if (!isLaunching) {
                isLaunching = true
                // Explicitly sign out cached Play Services account so that the Google Account
                // Picker bottom sheet / dialog is always displayed on every sign-in click.
                googleSignInClient.signOut()
                    .addOnCompleteListener {
                        try {
                            launcher.launch(googleSignInClient.signInIntent)
                        } catch (e: Exception) {
                            isLaunching = false
                            android.util.Log.e(
                                "RevLensAuth", "Failed to launch Google Sign-In intent", e
                            )
                        }
                    }
            }
        }
    }
}
