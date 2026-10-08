package com.example.revlens.ui.screens.navbarscreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SegmentedTabs
import com.example.revlens.ui.theme.RevLensTheme
import kotlinx.coroutines.launch
import com.example.revlens.ui.screens.detailscreens.BreakEvenScreen
import com.example.revlens.ui.screens.detailscreens.WhatIfScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToMetrics: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Simulator",
                actions = {
                    IconButton(onClick = onNavigateToMetrics) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Metrics",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SegmentedTabs(
                options = listOf("Pricing", "What-If", "Break-Even"),
                selectedIndex = pagerState.currentPage,
                onOptionSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PricingSimulatorScreen(viewModel = viewModel)
                    1 -> WhatIfScreen(viewModel = viewModel)
                    2 -> BreakEvenScreen(viewModel = viewModel)
                }
            }
        }
    }
}
