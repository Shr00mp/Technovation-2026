package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    allEntriesViewModel: AllJournalEntries,
    symptomsViewModel: SymptomsViewModel = viewModel(),
    medicationsViewModel: MedicationViewModel = viewModel(),
    resourcesViewModel: ResourcesViewModel = viewModel(),
    loginSignupViewmodel: LoginSignupViewmodel) {
    val recommendedArticles by resourcesViewModel.recommended.collectAsStateWithLifecycle()
    val topArticle = recommendedArticles.firstOrNull()
    var showDialog by remember {mutableStateOf(false)
    }
    if (showDialog) {
        AlreadyMadeEntryDialogue(
            onDismiss = {showDialog= false},
            onEdit = {
                var entry = allEntriesViewModel.history.first() // Store as something random first
                for (anEntry in allEntriesViewModel.history) {
                    if (anEntry.date == LocalDate.now()) {
                        val entry = anEntry
                        break
                    }
                }
                // Originally the symptoms are already togg [led for this entry so we needed to reset it
                // Otherwise old selections still apply when editing
                symptomsViewModel.resetSelections()
                symptomsViewModel.loadValuesForEditing(entry) // loads mood, text and date
                // Date is so that when creating new entry, it is saved to the correct date ("Finish Entry" button)

                // Users mentioned it was better to save previous selections instead of completely starting over
                entry.physicalSymptomsEntry.forEach { pastSymptom ->
                    symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.physicalSymptoms)
                }
                entry.mentalSymptomsEntry.forEach { pastSymptom ->
                    symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.mentalSymptoms)
                }
                entry.activitiesEntry.forEach { pastSymptom ->
                    symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.activities_list)
                }

                navController.navigate(AppPages.NewEntry.title)

                showDialog = false // close dialogue
            }
        )
    }
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Welcome back, ${loginSignupViewmodel.currentUserName}",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(20.dp))

        Text(
            "Have you journalled today?",
            fontSize = 25.sp,
            modifier=Modifier.align(Alignment.Start)
                .padding(20.dp, 15.dp)
        )

        Card(
            onClick = {
                if (allEntriesViewModel.hasEntryForDay(LocalDate.now())) {
                    showDialog = true
                } else {
                    symptomsViewModel.resetSelections()
                    navController.navigate(route = AppPages.NewEntry.title)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(25.dp)
            ) {
                Text( if (allEntriesViewModel.hasEntryForDay(LocalDate.now())) "Edit your entry for today"
                     else "Make your daily journal entry", fontSize=22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f))
                Spacer(modifier=Modifier.width(35.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier=Modifier.height(20.dp))

        Text(
            "Here is your next medication",
            fontSize = 25.sp,
            modifier=Modifier
                .align(Alignment.Start)
                .padding(20.dp, 15.dp)
        )

        if (medicationsViewModel.remainingTasks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(25.dp)
                ) {
                    Text("You have no more medications for today.", fontSize = 25.sp, textAlign = TextAlign.Center,)
                }
            }
        } else {
            MedicationTaskCard(
                currMedication = medicationsViewModel.remainingTasks[0],
                isCompleted = false,
                onDoneClick = {medicationsViewModel.markAsDone(medicationsViewModel.remainingTasks[0])})
        }

        Spacer(modifier=Modifier.height(20.dp))

        Text(
            "Your daily recommendation",
            fontSize = 25.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(20.dp, 15.dp)
        )

        if (topArticle != null) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                ArticleCard(
                    article = topArticle,
                    onClick = { navController.navigate("detail/${topArticle.contentId}") },
                    onBookmarkClick = { resourcesViewModel.toggleSaved(topArticle) }
                )
            }
        } else {
            // if no recommendations are found yet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Complete a journal entry to see personalized recommendations!",
                    modifier = Modifier.padding(25.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}