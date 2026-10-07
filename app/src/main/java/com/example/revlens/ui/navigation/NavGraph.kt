package com.example.revlens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.revlens.ui.dashboard.DashboardScreen

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
            // Placeholder for Input Data Screen
        }
        
        composable<WhatIfRoute> {
            // Placeholder for What-If Screen
        }
        
        composable<ForecastRoute> {
            // Placeholder for Forecast Screen
        }
        
        composable<GoalPlannerRoute> {
            // Placeholder for Goal Planner Screen
        }
    }
}
