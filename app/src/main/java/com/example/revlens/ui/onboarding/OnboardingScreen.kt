package com.example.revlens.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    var workspaceName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD ($)") } // Mocked dropdown

    Scaffold(
        containerColor = RevLensTheme.colors.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pager Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) RevLensTheme.colors.brandPrimary 
                                    else RevLensTheme.colors.borderDefault
                                )
                        )
                    }
                }

                PrimaryButton(
                    text = if (pagerState.currentPage == 0) "Continue" else "Create Workspace",
                    onClick = {
                        if (pagerState.currentPage < 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Skip button
            if (pagerState.currentPage == 0) {
                TextButton(
                    onClick = onFinishOnboarding,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Skip",
                        color = RevLensTheme.colors.brandPrimary,
                        style = RevLensTypography.labelLarge
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> CreateWorkspacePage(
                        workspaceName = workspaceName,
                        onWorkspaceNameChange = { workspaceName = it },
                        currency = currency,
                        onCurrencyChange = { currency = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Illustration Mock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(RevLensTheme.colors.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.width(120.dp).height(8.dp).background(RevLensTheme.colors.brandPrimary, CircleShape))
                Box(modifier = Modifier.width(90.dp).height(8.dp).background(RevLensTheme.colors.brandSecondary, CircleShape))
                Box(modifier = Modifier.width(60.dp).height(8.dp).background(RevLensTheme.colors.brandTertiary, CircleShape))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "See your revenue before it happens",
            style = RevLensTypography.displayLarge,
            color = RevLensTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureItem("Simulate price changes", RevLensTheme.colors.brandPrimary)
            FeatureItem("Compare scenarios", RevLensTheme.colors.brandSecondary)
            FeatureItem("Plan MRR goals", RevLensTheme.colors.brandTertiary)
        }
    }
}

@Composable
private fun FeatureItem(text: String, tint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = RevLensTypography.bodyLarge,
            color = RevLensTheme.colors.textSecondary
        )
    }
}

@Composable
private fun CreateWorkspacePage(
    workspaceName: String,
    onWorkspaceNameChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Name your workspace",
            style = RevLensTypography.displayLarge,
            color = RevLensTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Let's get started by setting up your first workspace.",
            style = RevLensTypography.bodyLarge,
            color = RevLensTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        RevLensTextField(
            value = workspaceName,
            onValueChange = onWorkspaceNameChange,
            label = "Workspace Name",
            placeholder = "e.g. Acme Corp"
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Simple text field to mock dropdown for now
        RevLensTextField(
            value = currency,
            onValueChange = onCurrencyChange,
            label = "Primary Currency",
            placeholder = "USD ($)"
        )
    }
}
