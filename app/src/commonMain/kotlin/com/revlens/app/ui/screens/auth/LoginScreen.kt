package com.revlens.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit = onNavigateToDashboard,
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    val googleSignInLauncher = rememberGoogleSignInLauncher { idToken, email, displayName ->
        viewModel.onEvent(LoginEvent.GoogleSignInClicked(idToken, email, displayName))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.text.asString())
                }

                LoginEffect.NavigateToDashboard -> {
                    onNavigateToDashboard()
                }

                LoginEffect.NavigateToCreateWorkspace -> {
                    onNavigateToCreateWorkspace()
                }
            }
        }
    }

    // Palette tokens tailored for modern dark/light SaaS
    val pageBg = if (isDark) Color(0xFF090D16) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF131826) else Color.White
    val cardBorder = if (isDark) BorderStroke(1.dp, Color(0xFF222B3D)) else BorderStroke(1.dp, Color(0xFFE2E8F0))
    val textPrimary = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val googleBtnBg = if (isDark) Color(0xFF1B2232) else Color.White
    val googleBtnBorder = if (isDark) BorderStroke(1.dp, Color(0xFF2F3B52)) else BorderStroke(1.dp, Color(0xFFCBD5E1))

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = pageBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFF1A2238),
                                Color(0xFF0F1422),
                                pageBg
                            )
                        } else {
                            listOf(
                                Color(0xFFEEF2FF),
                                Color(0xFFF1F5F9),
                                pageBg
                            )
                        },
                        center = Offset(200f, 150f),
                        radius = 900f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Central Auth Card (Minimal & Simple - No back button, no marketing clutter)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(cardBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 28.dp, vertical = 36.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Monogram Badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            RevLensTheme.colors.brandPrimary,
                                            RevLensTheme.colors.brandSecondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RL",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Sign in to RevLens",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.4).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in with Google to access your workspaces and models.",
                            style = RevLensTypography.bodyMedium,
                            color = textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Session Expired Banner
                        if (state.isSessionExpiredNotice) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Your previous session ended. Please sign in to resume.",
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                }
                            }
                        }

                        // Error Banner if present
                        if (state.error != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = state.error?.asString() ?: "Sign-in encountered an issue.",
                                        fontSize = 13.sp,
                                        color = textPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Primary Action: Google Sign-In Button
                        Button(
                            onClick = { googleSignInLauncher() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            border = googleBtnBorder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = googleBtnBg,
                                contentColor = textPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 1.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Connecting with Google...",
                                    style = RevLensTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary,
                                    fontSize = 14.sp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleIcon(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Google",
                                        style = RevLensTypography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "By signing in, you agree to our Terms of Service & Privacy Policy.",
                            color = textSecondary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = w * 0.18f

        // Google 4-color arcs
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            size = Size(w - stroke, h - stroke),
            topLeft = Offset(stroke / 2f, stroke / 2f)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            size = Size(w - stroke, h - stroke),
            topLeft = Offset(stroke / 2f, stroke / 2f)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            size = Size(w - stroke, h - stroke),
            topLeft = Offset(stroke / 2f, stroke / 2f)
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            size = Size(w - stroke, h - stroke),
            topLeft = Offset(stroke / 2f, stroke / 2f)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(w - stroke / 2f, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
    }
}
