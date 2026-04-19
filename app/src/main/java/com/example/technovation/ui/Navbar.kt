package com.example.technovation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.example.technovation.R

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
    ResourcesPage(title="Articles"),
    LoginPage(title="Login");

    fun createRouteForAddingMedication(id: Int): String {
        // Is for allowing the user to edit a medication task
        // id of that specific task is passed as part of the route, and can hence be given
        // as a parameter to the add medication function
        // Is cleaner and safer, I think, than only using temp variables in viewmodel
        return "$title/$id"
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?, // Determines the active tab
    onTabClick: (AppPages) -> Unit, // Handles tab click events
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        containerColor = Color(0xffDCE1DE)
    ) {
        val selectedColor = Color(0xff9CC5A1)
        //Home Page
        NavigationBarItem(
            selected = AppPages.HomePage.title == currentDestination?.route, // Mark as selected if current route matches Home route
            label = { Text(AppPages.HomePage.title) },
            icon = { Icon(Icons.Filled.Home, contentDescription = AppPages.HomePage.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = selectedColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.HomePage) }
        )

        // Journal Tab
        NavigationBarItem(
            selected = AppPages.Journal.title == currentDestination?.route,
            label = { Text(AppPages.Journal.title) },
            icon = { Icon(Icons.Filled.Edit, contentDescription = AppPages.Journal.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = selectedColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Journal) }
        )

        // Medication Tab
        NavigationBarItem(
            selected = AppPages.Medication.title == currentDestination?.route,
            label = { Text(AppPages.Medication.title) },
            icon = { Icon(Icons.Filled.Medication, contentDescription = AppPages.Medication.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = selectedColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Medication) }
        )

        // Articles tab
        NavigationBarItem(
            selected = AppPages.ResourcesPage.title == currentDestination?.route,
            label = { Text(AppPages.ResourcesPage.title) },
            icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = AppPages.ResourcesPage.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = selectedColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.ResourcesPage) }
        )

        // Record Audio Tab
        NavigationBarItem(
            selected = AppPages.Audio.title == currentDestination?.route,
            label = { Text(AppPages.Audio.title) },
            icon = { Icon(Icons.Filled.Mic, contentDescription = AppPages.Audio.title) },
            colors = NavigationBarItemColors(
                selectedIconColor = Color.Gray,
                selectedTextColor = Color.Gray,
                selectedIndicatorColor = selectedColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                disabledIconColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            onClick = { onTabClick(AppPages.Audio) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    currentDestination: NavDestination?,
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title={},
        navigationIcon = {
            // For when user first enters home page, there is nowhere back to go
            if (canNavigateBack) {
                IconButton(onClick = navigateBack, modifier=Modifier.size(50.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button",
                        modifier=Modifier.size(32.dp)
                    )
                }
            }
        },
        modifier = modifier,
        windowInsets = WindowInsets(0, 15, 0, 0)
    )
}