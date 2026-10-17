package com.example.revlens.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.revlens.ui.components.ActionCardButton
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.text.asString())
                }
                LoginEffect.GoToSignUp -> onNavigateToSignUp()
                LoginEffect.GoToForgotPassword -> onNavigateToForgotPassword()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (onNavigateBack != null) {
                RevLensTopAppBar(
                    title = "Sign In",
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RevLensTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Brand Pill / Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "RevLens SaaS Intelligence",
                        color = RevLensTheme.colors.brandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Title & Subtitle
                Text(
                    text = "Welcome back",
                    style = RevLensTypography.headlineLarge,
                    color = RevLensTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Log in to access your workspaces, forecasts, and team metrics.",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Session Expired Banner if applicable
                if (state.isSessionExpiredNotice) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = RevLensTheme.colors.warning.copy(alpha = 0.10f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = RevLensTheme.colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your session expired. Please sign in again.",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                }

                // Email Input
                RevLensTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                    label = "Email Address",
                    placeholder = "alex@company.com",
                    isError = state.emailError != null,
                    errorMessage = state.emailError?.asString(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input with Visibility Toggle
                RevLensTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                    label = "Password",
                    placeholder = "Enter your password",
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError?.asString(),
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) }) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                                tint = RevLensTheme.colors.textTertiary
                            )
                        }
                    }
                )

                // Forgot Password Link
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = { viewModel.onEvent(LoginEvent.ForgotPasswordClicked) }
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = RevLensTheme.colors.brandPrimary,
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Log In Button
                PrimaryButton(
                    text = "Sign In",
                    onClick = { viewModel.onEvent(LoginEvent.Submit) },
                    enabled = state.canSubmit,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RevLensTheme.colors.divider
                    )
                    Text(
                        text = "OR",
                        color = RevLensTheme.colors.textTertiary,
                        style = RevLensTypography.labelSmall,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RevLensTheme.colors.divider
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Continue as Guest Button
                ActionCardButton(
                    text = "Continue without account (Guest)",
                    onClick = { viewModel.onEvent(LoginEvent.ContinueAsGuestClicked) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Up Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account?",
                        color = RevLensTheme.colors.textSecondary,
                        style = RevLensTypography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.onEvent(LoginEvent.SignUpClicked) }) {
                        Text(
                            text = "Sign up",
                            color = RevLensTheme.colors.brandPrimary,
                            style = RevLensTypography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
