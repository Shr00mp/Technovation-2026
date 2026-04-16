package com.example.technovation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.technovation.R

data class User (
    val username: String,
    val password: String
)

class LoginSignupViewmodel : ViewModel() {
    val registeredUsers = mutableStateListOf<User>()
    var currentUserName by mutableStateOf("")

    fun signUp(user: User): Boolean {
        if (registeredUsers.any {it.username == user.username}) {
            return false
        } else {
            registeredUsers.add(user)
            currentUserName = user.username
            return true
        }
    }

    fun logIn(user: User): Boolean {
        if (registeredUsers.any {it.username == user.username && it.password == user.password}) {
            return true
        } else {
            return false
        }
    }
}

@Composable
fun LoginPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: LoginSignupViewmodel
) {
    // TabRow to display the tabs
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(50.dp))
        // Image for the app logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier
                .height(150.dp)
                .aspectRatio(1f)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(35.dp))
        Text("Welcome to Uplift",
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
            fontSize = 35.sp)

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Log in", "Sign up")

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .padding(70.dp, 30.dp)
                .align(alignment = Alignment.CenterHorizontally),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xff216869)
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color(0xff216869)
        ) {
            tabs.forEachIndexed { index, title -> // For each tab in the list:
                Tab(
                    selected = selectedTab == index, // Determines whether or not tab is selected
                    onClick = { selectedTab = index }, // When clicked, the tab is selected
                    text = { // Displays the tab title
                        Text(
                            title,
                            fontSize = 20.sp,
                            modifier = Modifier.absolutePadding(0.dp, 0.dp, 0.dp, 5.dp)
                        )
                    },
                    selectedContentColor = Color(0xff216869)
                )
            }
        }

        // Content for each tab, specified using the Tab Index
        when (selectedTab) {
            0 -> LoginContent(navController = navController, viewModel)
            1 -> SignupContent(navController = navController, viewModel)
        }
    }

}

// Displayed when the Login tab is selected
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(navController: NavController, viewModel: LoginSignupViewmodel) {
    var username by remember { mutableStateOf("") } // Stores entered username
    var password by remember { mutableStateOf("") } // Stores entered password
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))

        // Username text field
        OutlinedTextField(
            value = username,
            onValueChange = {username = it}, // As user types, the username variable also updates
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "person") // Icon at the front of the text field is a person
            },
            label = {Text(text = "Username")},
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp),
        )
        Spacer(modifier = Modifier.height(15.dp))

        // Password text field
        OutlinedTextField(
            value = password,
            onValueChange = {password = it}, // As user types, the password variable also updates
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "lock")
            },
            label = {Text(text = "Password")},
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp),
        )

        var errorMessage by remember { mutableStateOf("") } // At first, there is no error message
        ErrorMessage(errorMessage = errorMessage) // Show error message
        Spacer(modifier = Modifier.height(50.dp))

        // Login button
        Button(
            onClick = {
                errorMessage = getErrorMessage( // Get the right error message
                    type = "Log in",
                    username = username,
                    password = password,
                    confirmPassword = "")
                if (errorMessage == "") { // If there is no error
                    val success = viewModel.logIn(User(username, password))
                    if (success) {
                        navController.navigate(AppPages.HomePage.title)
                    } else {
                        errorMessage = "Invalid username or password."
                    }
                }
            },
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(Color(0xff216869))
        ) {
            Text("Log in", fontSize = 20.sp, color = Color.White)
        }
    }

}

// Helper function to determine what the error message should be
fun getErrorMessage(type: String, username: String, password: String, confirmPassword: String): String {
    var errorMessage = ""
    if (username == "") { // If username has not been entered
        errorMessage = "Please enter a username."
    }
    else if (password == "") { // If passowrd has not been enetered
        errorMessage = "Please enter a password"
    }
    else if (type == "Sign up") { // If the sign up tab is selected and
        if (password != confirmPassword) { // the passwords do not match
            errorMessage = "Passwords do not match"
        }
    }
    return errorMessage
}

// Function for displaying the error message
@Composable
fun ErrorMessage(errorMessage: String) {
    if (errorMessage != "") { // If error message is empty, nothing shows
        Spacer(modifier = Modifier.height(50.dp))
        Column (modifier = Modifier.fillMaxWidth()) {
            Card (
                modifier = Modifier
                    .width(400.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor =MaterialTheme.colorScheme.error
                )
            ) {
                Row(modifier = Modifier.padding(20.dp, 20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "lock",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp))
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.absolutePadding(13.dp))
                }
            }
        }
    }
}

// Displayed when the Login tab is selected
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupContent(navController: NavController, viewmodel: LoginSignupViewmodel) {
    var username by remember { mutableStateOf("") } // Stores entered username
    var password by remember { mutableStateOf("") } // Stores entered password
    var confirmPassword by remember { mutableStateOf("") } // Stores entered second password (for confirmation)

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {username = it}, // As user types, the username also updates
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "person")
            },
            label = {Text(text = "Username")},
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp),
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {password = it}, // As the user types, the password also updates
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "lock")
            },
            label = {Text(text = "Password")},
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp),
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {confirmPassword = it}, // As the user types, the confirmation password also updates
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "lock")
            },
            label = {Text(text = "Confirm password")},
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp),
        )

        var errorMessage by remember { mutableStateOf("") } // There is no error message at first
        ErrorMessage(errorMessage = errorMessage) // Display the error message
        Spacer(modifier = Modifier.height(50.dp))

        // Sign up button
        Button(
            onClick = {
                errorMessage = getErrorMessage( // Get the right error message
                    type = "Sign up",
                    username = username,
                    password = password,
                    confirmPassword = confirmPassword)
                if (errorMessage == "") { // If there is no error message
                    val success = viewmodel.signUp(User(username, password))
                    if (success) {
                        navController.navigate(AppPages.HomePage.title)
                    } else {
                        errorMessage = "An account with this username already exists."
                    }
                }
            },
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .width(400.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(Color(0xff216869))
        ) {
            Text("Sign up", fontSize = 20.sp, color = Color.White)
        }
    }
}