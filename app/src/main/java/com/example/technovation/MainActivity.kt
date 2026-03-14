package com.example.technovation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.technovation.ui.AlreadyMadeEntry
import com.example.technovation.ui.JournalPage
import com.example.technovation.ui.NewJournalEntry
import com.example.technovation.ui.PastEntries
import com.example.technovation.ui.TechnovationApp
import com.example.technovation.ui.theme.TechnovationTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechnovationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TechnovationApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}