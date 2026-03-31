package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TechnovationApp(
    modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val allJournalEntries: AllJournalEntries = viewModel()

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
                JournalPage(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries)
            }
            composable(route = AppPages.NewEntry.title) {
                NewJournalEntry(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries)
            }
            composable(route = AppPages.PastEntries.title) {
                PastEntries(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries)
            }
            composable(route = AppPages.Stats.title) {
                StatisticsPage(
                    navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries
                )
            }
            composable(route = AppPages.Medication.title) {
                MedicationPage(
                    navController = navController,
                    modifier = modifier
                )
            }
            composable(route = AppPages.Audio.title) {
                AudioPage(
                    navController = navController,
                    modifier = modifier
                )
            }
            composable(route = AppPages.MakeRecording.title) {
                MakeRecording(
                    navController = navController,
                    modifier = modifier
                )
            }
        }
    }
}