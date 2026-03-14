package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.ModifierLocal
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    allEntriesViewModel: AllJournalEntries = viewModel(),
    symptomsViewModel: SymptomsViewModel = viewModel()) {
    var showDialog by remember {mutableStateOf(false)}
    if (showDialog) {
        QuickDialogue(onDismiss = {showDialog= false})
    }
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Your Journal",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier=Modifier.height(40.dp))

        Button(
            onClick = {
                if (allEntriesViewModel.hasEntryForDay(LocalDate.now())) {
                    showDialog = true
                } else {
                    symptomsViewModel.resetSelections()
                    navController.navigate(route = AppPages.NewEntry.title)
                }
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Make a new entry", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Button(
            onClick = {navController.navigate(route = AppPages.PastEntries.title)},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("See past entries", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("See your statistics", fontSize=20.sp)
        }
    }
}

data class Symptom(
    val id: Int,
    val name: String,
    var selected: Boolean = false
)

class SymptomsViewModel: ViewModel() {
    var physical_symptoms = mutableStateListOf<Symptom>(
        Symptom(1, "Dizziness"),
        Symptom(2, "Loss of smell"),
        Symptom(3, "Headaches"),
        Symptom(4, "Insomnia")
    )

    var mental_symptoms = mutableStateListOf<Symptom>(
        Symptom(1, "Anxious"),
        Symptom(2, "Depressed"),
        Symptom(3, "Tired"),
        Symptom(4, "Irritated")
    )

    var activities_list = mutableStateListOf<Symptom>(
        Symptom(1, "Yoga"),
        Symptom(2, "Meditation"),
        Symptom(3, "Walking"),
        Symptom(4, "Dancing")
    )

    // Is for selecting the Symptom with provided id and in one of the three lists
    fun toggleSymptom(id: Int, curr_list: MutableList<Symptom>) {
        val index = curr_list.indexOfFirst { it.id == id }
        var temp = curr_list[index]
        // need to replace the object with an entirely new copy
        curr_list[index] = temp.copy(selected = !temp.selected)
    }

    fun getSelectedPhysicalSymptoms(): List<Symptom> {
        var return_list = mutableListOf<Symptom>()
        for (symptom in physical_symptoms) {
            if (symptom.selected) {
                return_list.add(symptom)
            }
        }
        return return_list
    }

    fun getSelectedMentalSymptoms(): List<Symptom> {
        var return_list = mutableListOf<Symptom>()
        for (symptom in mental_symptoms) {
            if (symptom.selected) {
                return_list.add(symptom)
            }
        }
        return return_list
    }

    fun getSelectedActivities(): List<Symptom> {
        var return_list = mutableListOf<Symptom>()
        for (symptom in activities_list) {
            if (symptom.selected) {
                return_list.add(symptom)
            }
        }
        return return_list
    }

    fun resetSelections() {
        for (i in physical_symptoms.indices) {
            physical_symptoms[i] = physical_symptoms[i].copy(selected = false)
        }
        for (i in mental_symptoms.indices) {
            mental_symptoms[i] = mental_symptoms[i].copy(selected = false)
        }
        for (i in activities_list.indices) {
            activities_list[i] = activities_list[i].copy(selected = false)
        }
    }
}

@Composable
fun SymptomItem(symptom: Symptom, toggleSymptom: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The Status Circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (symptom.selected) Color.Gray else Color.Green) // Changes color
                .clickable(enabled = true, onClick = toggleSymptom)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = symptom.name,
            fontSize = 18.sp
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewJournalEntry(
    modifier: Modifier = Modifier,
    symptomsViewModel: SymptomsViewModel = viewModel(),
    allEntriesViewModel: AllJournalEntries = viewModel(),
    navController: NavController) {
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier=Modifier.height(40.dp))

        Text(
            "Make a New Entry",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier=Modifier.height(40.dp))

        // Physical symptoms card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Add physical symptoms",
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier=Modifier.height(10.dp))

            symptomsViewModel.physical_symptoms.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { symptomsViewModel.toggleSymptom(
                        symptom.id,
                        curr_list = symptomsViewModel.physical_symptoms)}
                )
            }
        }

        Spacer(modifier=Modifier.height(30.dp))

        // Mental symptoms card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Add mental symptoms",
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier=Modifier.height(10.dp))

            symptomsViewModel.mental_symptoms.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { symptomsViewModel.toggleSymptom(
                        symptom.id,
                        curr_list = symptomsViewModel.mental_symptoms)}
                )
            }
        }

        Spacer(modifier=Modifier.height(30.dp))

        // Activities card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Add activities",
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier=Modifier.height(10.dp))

            symptomsViewModel.activities_list.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { symptomsViewModel.toggleSymptom(
                        symptom.id,
                        curr_list = symptomsViewModel.activities_list)}
                )
            }
        }

        Spacer(modifier=Modifier.height(30.dp))

        var journal_text by remember {mutableStateOf("")}
        TextField(
            value=journal_text,
            onValueChange = { journal_text = it },
            label = { Text("Write something about how your day went") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier=Modifier.height(30.dp))

        Button(
            onClick = {
                val newEntry = Entry(
                    date = LocalDate.now(),
                    physical_symptoms_entry = symptomsViewModel.getSelectedPhysicalSymptoms(),
                    mental_symptoms_entry = symptomsViewModel.getSelectedMentalSymptoms(),
                    activities_entry = symptomsViewModel.getSelectedActivities(),
                    text_in_journal = journal_text
                )
                allEntriesViewModel.addEntry(newEntry)
                navController.navigate(AppPages.Journal.title)
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Finish entry", fontSize=20.sp)
        }
    }
}


data class Entry(
    val date: LocalDate,
    val physical_symptoms_entry: List<Symptom>,
    val mental_symptoms_entry: List<Symptom>,
    val activities_entry: List<Symptom>,
    val text_in_journal: String,
)

@RequiresApi(Build.VERSION_CODES.O)
class AllJournalEntries: ViewModel() {
    val history = mutableStateListOf<Entry>()

    init {
        // This data is just for testing
        history.add(
            Entry(
                date = LocalDate.of(2026, 2, 3), // 03/02/2026
                physical_symptoms_entry = listOf(
                    Symptom(1, "Dizziness", true),
                    Symptom(1, "Headaches", true)
                ),
                mental_symptoms_entry = listOf(
                    Symptom(1, "Anxious", true)
                ),
                activities_entry = listOf(
                    Symptom(1, "Meditation", true)
                ),
                text_in_journal = "Today I felt a bit lightheaded in the morning, but meditation helped."
            )
        )
        history.add(
            Entry(
                date = LocalDate.of(2026, 1, 10), // 03/02/2026
                physical_symptoms_entry = listOf(
                    Symptom(1, "Loss of smell", true),
                    Symptom(1, "Cramps", true),
                    Symptom(1, "Tremors", true)
                ),
                mental_symptoms_entry = listOf(
                    Symptom(1, "Depressed", true),
                    Symptom(1, "Tired", true)
                ),
                activities_entry = listOf(
                ),
                text_in_journal = "Was too tired today to do any exercise."
            )
        )
        history.add(
            Entry(
                date = LocalDate.of(2026, 1, 4), // 03/02/2026
                physical_symptoms_entry = listOf(
                    Symptom(1, "Insomnia", true),
                    Symptom(3, "Tremors", true),
                ),
                mental_symptoms_entry = listOf(
                    Symptom(1, "Depressed", true),
                    Symptom(2, "Groggy", true),
                    Symptom(3, "Anxious", true)
                ),
                activities_entry = listOf(
                    Symptom(1, "Yoga", true),
                    Symptom(1, "Walk", true),
                ),
                text_in_journal = "Today I didn't sleep very well and felt quite groggy. I've been " +
                        "anxious about my sleep but going on a walk and doing some yoga made me feel" +
                        " a bit less depressed."
            )
        )
    }

    fun addEntry(entry: Entry) {
        history.add(0, entry)
    }

    fun hasEntryForDay(date_to_check: LocalDate): Boolean {
        for (entry in history) {
            if (entry.date == date_to_check) {
                return true
            }
        }
        return false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastEntryCard(entry: Entry) {
    var day_of_week = entry.date.dayOfWeek
    var month = entry.date.month
    var year = entry.date.year
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 0.dp, 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier= Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "$day_of_week, $month $year"
            )

            Spacer(modifier=Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.physical_symptoms_entry.forEach { symptom ->
                    Text(symptom.name + " ")
                }
            }

            Spacer(modifier=Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.mental_symptoms_entry.forEach { symptom ->
                    Text(symptom.name + " ")
                }
            }

            Spacer(modifier=Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.activities_entry.forEach { symptom ->
                    Text(symptom.name + " ")
                }
            }

            Spacer(modifier=Modifier.height(20.dp))

            Text(entry.text_in_journal)

            Spacer(modifier=Modifier.height(20.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastEntries(
    modifier: Modifier = Modifier,
    allEntriesViewModel: AllJournalEntries = viewModel(),
    navController: NavController
) {
    Column(modifier=modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top)
    {
        Spacer(modifier=Modifier.height(40.dp))

        Text(
            "All Past Entries",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier=Modifier.height(40.dp))

        if (allEntriesViewModel.history.isEmpty()) {
            Text("No entries yet. Start journaling!")
        } else {
            allEntriesViewModel.history.forEach { entry ->
                PastEntryCard(entry)
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun QuickDialogue(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "You have already made an entry today. See you again tomorrow!",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}


