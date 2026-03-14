package com.example.technovation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun TechnovationApp(
    modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentDestination,
                onTabClick = {
                    navController.navigate(it.title)
                },
                modifier
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppPages.Journal.title,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppPages.Journal.title) {
                JournalPage(navController = navController, modifier = modifier)
            }
        }
    }
}