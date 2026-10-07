package com.example.revlens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.revlens.ui.dashboard.DashboardScreen
import com.example.revlens.ui.setup.BusinessSetupScreen
import com.example.revlens.ui.whatif.WhatIfScreen

@Composable
fun RevLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToInput = {
                    navController.navigate(InputDataRoute)
                },
                onNavigateToWhatIf = {
                    navController.navigate(WhatIfRoute)
                }
            )
        }
        
        composable<InputDataRoute> {
            BusinessSetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<WhatIfRoute> {
            WhatIfScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<ForecastRoute> {
            // Placeholder for Forecast Screen
        }
        
        composable<GoalPlannerRoute> {
            // Placeholder for Goal Planner Screen
        }
    }
}
