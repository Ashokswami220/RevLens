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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SegmentedTabs
import com.example.revlens.ui.screens.detailscreens.BreakEvenScreen
import com.example.revlens.ui.screens.detailscreens.WhatIfScreen
import com.example.revlens.ui.theme.RevLensTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    initialTab: Int = 0,
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToMetrics: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(0, 2),
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    androidx.compose.runtime.LaunchedEffect(initialTab) {
        if (pagerState.currentPage != initialTab) {
            pagerState.scrollToPage(initialTab.coerceIn(0, 2))
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                RevLensTopAppBar(
                    title = "Simulator",
                    scrollBehavior = scrollBehavior,
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
                SegmentedTabs(
                    options = listOf("Pricing", "What-If", "Break-Even"),
                    selectedIndex = pagerState.currentPage,
                    onOptionSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> PricingSimulatorScreen(viewModel = viewModel)
                1 -> WhatIfScreen(viewModel = viewModel)
                2 -> BreakEvenScreen(viewModel = viewModel)
            }
        }
    }
}
