package com.example.revlens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevLensTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = RevLensTypography.labelMedium,
            color = RevLensTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textTertiary
                    )
                }
            },
            isError = isError,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = RevLensTheme.colors.surfaceElevated,
                focusedContainerColor = RevLensTheme.colors.surfaceElevated,
                focusedBorderColor = RevLensTheme.colors.primaryAction,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = RevLensTheme.colors.error,
                focusedTextColor = RevLensTheme.colors.textPrimary,
                unfocusedTextColor = RevLensTheme.colors.textPrimary,
                cursorColor = RevLensTheme.colors.textPrimary
            ),
            singleLine = true,
            textStyle = RevLensTypography.bodyLarge
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    RevLensTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                    tint = RevLensTheme.colors.textSecondary
                )
            }
        }
    )
}

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    RevLensTextField(
        value = value,
        onValueChange = {
            // Simple validation to only allow numbers and decimal point
            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                onValueChange(it)
            }
        },
        label = label,
        modifier = modifier,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = {
            if (suffix != null) {
                Text(
                    text = suffix,
                    style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    )
}
