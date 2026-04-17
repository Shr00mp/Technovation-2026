package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TechnovationApp(
    modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val allJournalEntries: AllJournalEntries = viewModel()
    val allAudioResults: AllAudioResults = viewModel()
    val resourcesViewModel: ResourcesViewModel = viewModel()
    val allMedicationsViewModel: MedicationViewModel = viewModel()
    val symptomsViewModel: SymptomsViewModel = viewModel()
    val loginSignupViewmodel: LoginSignupViewmodel = viewModel()
    val canNavigateBack = navController.previousBackStackEntry != null

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != AppPages.LoginPage.title) {
                BottomNavigationBar(
                    currentDestination,
                    onTabClick = {
                        navController.navigate(it.title)
                    },
                    modifier
                )
            }
        },
        topBar = {
            if (currentDestination?.route != AppPages.LoginPage.title) {
                TopNavigationBar(
                    currentDestination = currentDestination,
                    canNavigateBack = canNavigateBack,
                    navigateBack = { navController.popBackStack()}
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppPages.HomePage.title,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppPages.LoginPage.title) {
                LoginPage(navController = navController,
                    modifier = modifier,
                    viewModel = loginSignupViewmodel)
            }
            composable(route = AppPages.HomePage.title) {
                Home(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries,
                    medicationsViewModel = allMedicationsViewModel,
                    resourcesViewModel = resourcesViewModel,
                    loginSignupViewmodel = loginSignupViewmodel)
            }
            composable(route = AppPages.ResourcesPage.title){
                ResourcesPage(navController = navController,
                    viewModel = resourcesViewModel,
                    allEntriesViewModel = allJournalEntries)
            }
            composable(
                route = "detail/{contentId}",
                arguments = listOf(navArgument("contentId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("contentId") ?: return@composable
                ArticleDetailScreen(
                    contentId = id,
                    viewModel = resourcesViewModel,
                    navController = navController
                )
            }
            composable(route = AppPages.Journal.title) {
                JournalPage(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries,
                    symptomsViewModel = symptomsViewModel)
            }
            composable(route = AppPages.NewEntry.title) {
                NewJournalEntry(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries,
                    symptomsViewModel = symptomsViewModel)
            }
            composable(route = AppPages.PastEntries.title) {
                PastEntries(navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries,
                    symptomsViewModel = symptomsViewModel)
            }
            composable(route = AppPages.Stats.title) {
                StatisticsPage(
                    navController = navController,
                    modifier = modifier,
                    allEntriesViewModel = allJournalEntries,
                    symptomsViewModel = symptomsViewModel
                )
            }
            composable(route = AppPages.Medication.title) {
                MedicationPage(
                    navController = navController,
                    modifier = modifier,
                    viewModel = allMedicationsViewModel
                )
            }
            composable(route = AppPages.ManageMedications.title) {
                ManageMedicationPage(
                    navController = navController,
                    modifier = modifier,
                    viewModel = allMedicationsViewModel
                )
            }
            composable(
                route = "${AppPages.AddMedications.title}/{medicationId}",
                arguments = listOf(navArgument("medicationId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("medicationId") ?: -1
                AddMedicationPage(
                    navController = navController,
                    viewModel = allMedicationsViewModel,
                    medicationId = id
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
                    modifier = modifier,
                    allResultsViewmodel = allAudioResults
                )
            }
            composable(route = AppPages.PastRecordings.title) {
                AllPastRecordings(
                    navController = navController,
                    modifier = modifier,
                    allAudioResults = allAudioResults
                )
            }
        }
    }
}