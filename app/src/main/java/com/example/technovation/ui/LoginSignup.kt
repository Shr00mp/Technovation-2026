package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WelcomePage(
    modifier: Modifier = Modifier,
    //navController: NavController
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to our app!", fontSize = 30.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {

        }) {
            Text(text = "Log In", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {

        }) {
            Text(text = "Sign Up", fontSize = 20.sp)
        }
    }
}