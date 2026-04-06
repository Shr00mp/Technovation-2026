package com.example.technovation.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination

enum class AppPages(var title: String) {
    Journal(title = "Journal"),
    NewEntry(title = "NewEntry"),
    PastEntries(title = "PastEntries"),
    Stats(title="Stats"),
    Medication(title="Medication"),
    ManageMedications(title="Manage Medications"),
    AddMedications(title = "Add Medications"),
    Audio(title="Audio"),
    MakeRecording(title="Make Recording"),
    PastRecordings(title="Past Recordings"),
    HomePage(title="Home"),
    ResourcesPage(title="Resources")
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?, // Determines the active tab
    onTabClick: (AppPages) -> Unit, // Handles tab click events
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
    ) {
        //Home Page
        NavigationBarItem(
            selected = AppPages.HomePage.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.HomePage.title) },
            icon = { Icon(Icons.Filled.Home, contentDescription = AppPages.HomePage.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = Color.Blue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.HomePage) }
        )

        NavigationBarItem(
            selected = AppPages.ResourcesPage.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.ResourcesPage.title) },
            icon = { Icon(Icons.Filled.Home, contentDescription = AppPages.ResourcesPage.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = Color.Blue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.ResourcesPage) }
        )

        // Journal Tab
        NavigationBarItem(
            selected = AppPages.Journal.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.Journal.title) },
            icon = { Icon(Icons.Filled.DateRange, contentDescription = AppPages.Journal.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = Color.Blue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Journal) }
        )

        // Medication Tab
        NavigationBarItem(
            selected = AppPages.Medication.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.Medication.title) },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = AppPages.Medication.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = Color.Blue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Medication) }
        )

        // Record Audio Tab
        NavigationBarItem(
            selected = AppPages.Audio.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.Audio.title) },
            icon = { Icon(Icons.Filled.Person, contentDescription = AppPages.Audio.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = Color.Blue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Audio) }
        )
    }
}