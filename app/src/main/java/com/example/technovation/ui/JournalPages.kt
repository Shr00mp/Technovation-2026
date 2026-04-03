package com.example.technovation.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.DayOfWeek
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
        AlreadyMadeEntryDialogue(onDismiss = {showDialog= false})
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
            onClick = {navController.navigate(route = AppPages.Stats.title)},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("See your statistics", fontSize=20.sp)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
data class Symptom(
    val id: Int,
    val name: String,
    var selected: Boolean = false,
    val dateAdded: LocalDate = LocalDate.now()
)

@RequiresApi(Build.VERSION_CODES.O)
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

    val allSymptomsNames = (physical_symptoms + mental_symptoms).map { it.name }

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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun addNewSymptom(symptomName: String, type: Int) {
        // 1 = physical symptom
        // 2 = mental symptom
        // 3 = an activity
        when (type) {
            1 -> {
                val newSymptom = Symptom(id = physical_symptoms.size, name = symptomName, selected = false)
                physical_symptoms.add(newSymptom)
            }
            2 -> {
                val newSymptom = Symptom(id = mental_symptoms.size, name = symptomName, selected = false)
                mental_symptoms.add(newSymptom)
            }
            else -> {
                val newSymptom = Symptom(id = activities_list.size, name = symptomName, selected = false)
                activities_list.add(newSymptom)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateImprovement(history: List<Entry>, activity: Symptom, symptomName: String): Double? {
        // Improvement is calculated by comparing the number of times a symptom appears before and after starting an activity
        // If the percentage change is significant enough, this represents there being an improvement
        // This "percentage" is in terms of how often the symptom appears in the total number of days

        val beforeEntries = history.filter { it.date.isBefore(activity.dateAdded) }
        val afterEntries = history.filter { it.date.isAfter(activity.dateAdded) || it.date.isEqual(activity.dateAdded) }

        // Should have at least some entries before and after the activity started to make a comparison
        if (beforeEntries.isEmpty() || afterEntries.isEmpty()) return null

       // Get the regularity of symptom before activity using (total days with symptom) / (total days without symptom)
        val frequencyBefore = beforeEntries.count { entry ->
            entry.mental_symptoms_entry.any { it.name == symptomName } ||
                    entry.physical_symptoms_entry.any { it.name == symptomName }
        }.toDouble() / beforeEntries.size

        // Get the regularity of the symptom after activity
        val frequencyAfter = afterEntries.count { entry ->
            entry.mental_symptoms_entry.any { it.name == symptomName } ||
                    entry.physical_symptoms_entry.any { it.name == symptomName }
        }.toDouble() / afterEntries.size

        // Return the difference
        return (frequencyBefore - frequencyAfter) * 100
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeeklySymptomCounts(history: List<Entry>, symptomName: String): Map<LocalDate, Int> {
        if (history.isEmpty() || symptomName.isBlank()) return emptyMap()

        val target = symptomName.trim()

        return history.groupBy { entry ->
            entry.date.with(DayOfWeek.MONDAY)
        }.mapValues { (_, entriesInWeek) ->
            entriesInWeek.count { entry ->
                entry.physical_symptoms_entry.any { it.name.equals(target, ignoreCase = true) } ||
                        entry.mental_symptoms_entry.any { it.name.equals(target, ignoreCase = true) }
            }
        }.toSortedMap()
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


@Composable
fun AddNewSymptomDialogue(onDismissRequest: () -> Unit, type: Int, onConfirm: (String) -> Unit) {
    var symptomName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (type) {
                    1 -> {
                        Text("Enter a new physical symptom:")
                    }
                    2 -> {
                        Text("Enter a new mental symptom:")
                    }
                    3 -> {
                        Text("Enter an activity:")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = symptomName,
                    onValueChange = { symptomName = it },
                    label = { Text("Name") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (symptomName.isNotBlank()){
                        onConfirm(symptomName)
                        onDismissRequest()
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewJournalEntry(
    modifier: Modifier = Modifier,
    symptomsViewModel: SymptomsViewModel = viewModel(),
    allEntriesViewModel: AllJournalEntries = viewModel(),
    navController: NavController) {
        var journalText by remember {mutableStateOf("")}
        val voiceInputHandler = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if(activityResult.resultCode == Activity.RESULT_OK) {
                val resultText = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                resultText?.get(0)?.let{ recordedText ->
                    journalText += recordedText
                }
            }
        }
        val context = LocalContext.current
        val language = "en"

    fun voiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice to text")

        try {
            voiceInputHandler.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Sorry, we couldn't catch that!", Toast.LENGTH_SHORT).show()
        }
    }

    // State for tracking dialogue (0 = none, 1 = physical, 2 = mental, 3 = activity)
    var activeDialogType by remember { mutableStateOf(0) }
    var selectedMood by remember {mutableStateOf(3)} // default mood is average

    // Dialogue does not show only when state is 0
    if (activeDialogType != 0) {
        AddNewSymptomDialogue(
            type = activeDialogType,
            onDismissRequest = { activeDialogType = 0 },
            onConfirm = { name ->
                symptomsViewModel.addNewSymptom(name, activeDialogType)
            }
        )
    }

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier=Modifier.height(40.dp))

        // Title
        Text(
            "Make a New Entry",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier=Modifier.height(40.dp))

        // Mood card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "How are you feeling on a scale from 1 to 5? (1 being the worst and 5 being the best)",
                modifier = Modifier.padding(16.dp)
            )
            val moodEmojis = listOf("😢", "😟", "😐", "🙂", "😄")
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom=20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodEmojis.forEachIndexed { index, emoji ->
                    val level = index + 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = emoji,
                            fontSize = 30.sp,
                            modifier = Modifier
                                .clickable { selectedMood = level }
                                .alpha(if (selectedMood == level) 1f else 0.3f)
                        )
                    }
                }
            }
        }

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

            Button(
                onClick = { activeDialogType = 1 },
                modifier = Modifier
                    .height(50.dp)
                    .width(350.dp),
            ) {
                Text("Add new physical symptom", fontSize=20.sp)
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

            Button(
                onClick = { activeDialogType = 2 },
                modifier = Modifier
                    .height(50.dp)
                    .width(350.dp),
            ) {
                Text("Add new physical symptom", fontSize=20.sp)
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

            Button(
                onClick = { activeDialogType = 3 },
                modifier = Modifier
                    .height(50.dp)
                    .width(350.dp),
            ) {
                Text("Add new physical symptom", fontSize=20.sp)
            }

        }

        Spacer(modifier=Modifier.height(30.dp))

        //The layout of this is ugly for now but it works
        Card(modifier = Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)){
            TextField(
                value=journalText,
                onValueChange = { journalText = it },
                label = { Text("Write about how your day went") },
                modifier = Modifier.padding(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray,
                    unfocusedContainerColor = Color.Green
                )
            )
            Text(
                text = "Press the button below for voice to text",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = { voiceInput() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    //For now as there is no mic icon
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone"
                )
            }
        }

        Spacer(modifier=Modifier.height(30.dp))

        Button(
            onClick = {
                val newEntry = Entry(
                    date = LocalDate.now(),
                    mood = selectedMood,
                    physical_symptoms_entry = symptomsViewModel.getSelectedPhysicalSymptoms(),
                    mental_symptoms_entry = symptomsViewModel.getSelectedMentalSymptoms(),
                    activities_entry = symptomsViewModel.getSelectedActivities(),
                    text_in_journal = journalText
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
    val mood: Int,
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
                mood = 2,
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
                mood = 1,
                physical_symptoms_entry = listOf(
                    Symptom(1, "Loss of smell", true),
                    Symptom(1, "Insomnia", true),
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
                mood = 4,
                physical_symptoms_entry = listOf(
                    Symptom(1, "Insomnia", true),
                    Symptom(3, "Headaches", true),
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

            val moodEmojis = listOf("😢", "😟", "😐", "🙂", "😄")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(moodEmojis[entry.mood - 1])
            }

            Spacer(modifier=Modifier.height(5.dp))

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
fun AlreadyMadeEntryDialogue(
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodChart(entries: List<Entry>, modifier: Modifier = Modifier) {
    val maxMood = 5f
    val moodEmojis = listOf("😢", "😟", "😐", "🙂", "😄")
    val sortedEntries = entries.sortedBy { it.date }
    // Note that the coordinate system has (0, 0) as the top left corner

    Canvas(modifier = modifier) {
        val leftPadding = 60.dp.toPx() // Creates space for emojis
        val bottomPadding = 50.dp.toPx() // Creates a lane for the dates
        val topPadding = 30.dp.toPx()
        val rightPadding = 20.dp.toPx()

        // Get the actual width and height of the chart
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - bottomPadding - topPadding

        // Gets the number of pixels between each day
        val xStep = if (sortedEntries.size > 1) chartWidth / (sortedEntries.size - 1) else 0f

        // Gets the x and y coordinate of each of the points
        val points = sortedEntries.mapIndexed { index, entry ->
            val x = leftPadding + (index * xStep)
            val y = topPadding + (chartHeight - ((entry.mood - 1) / (maxMood - 1)) * chartHeight)
            Offset(x, y)
        }

        // For drawing the emojis
        val paint = Paint().apply {
            textSize = 24.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        // Draws the 5 horizontal grid lines
        for (i in 0 until 5) {
            val y = topPadding + (chartHeight - (i / 4f) * chartHeight)
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.2f),
                start = Offset(leftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(moodEmojis[i], leftPadding / 2, y + (paint.textSize / 3), paint)
            }
        }

        // For drawing the dates
        val datePaint = Paint().apply {
            textSize = 18.sp.toPx() // Shrunk from 24sp
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
        }

        // Plots the dates for each entry
        sortedEntries.forEachIndexed { index, entry ->
            val x = leftPadding + (index * xStep)
            val dateLabel = "${entry.date.monthValue}/${entry.date.dayOfMonth}"
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(dateLabel, x, size.height - 10.dp.toPx(), datePaint)
            }
        }

        // Draw the line between each point
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF6200EE),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw the circle for each point
        points.forEach { point ->
            drawCircle(color = Color(0xFF3700B3), radius = 4.dp.toPx(), center = point)
        }
    }
}

// Template from the Android Developer components list
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { Text("Search for a symptom") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onSearch(query)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    // ADDED LOGIC HERE:
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                onQueryChange("") // Clears the text
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            LazyColumn {
                items(count = searchResults.size) { index ->
                    val resultText = searchResults[index]
                    ListItem(
                        headlineContent = { Text(resultText) },
                        supportingContent = supportingContent?.let { { it(resultText) } },
                        leadingContent = leadingContent,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable {
                                onResultClick(resultText)
                                expanded = false
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SymptomGraphCard(history: List<Entry>, viewModel: SymptomsViewModel) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val allNames = viewModel.allSymptomsNames
    val filteredResults = allNames.filter { it.contains(searchQuery, ignoreCase = true) }
    val weeklyData = viewModel.getWeeklySymptomCounts(history, searchQuery)

    Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Your Progress for a Specific Symptom",
                fontSize = 20.sp,
                modifier=Modifier
                    .align(Alignment.CenterHorizontally)
            )

            // Container for search bar
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                CustomizableSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        searchQuery = it
                    },
                    searchResults = filteredResults,
                    onResultClick = { selectedName ->
                        searchQuery = selectedName
                    }
                )
            }

            // Note that graph only shown if search bar not expanded
            // This prevents the graph from being pushed off the screen
            if (searchQuery.isNotBlank() && allNames.contains(searchQuery)) {
                Spacer(modifier = Modifier.height(20.dp))

                if (weeklyData.isNotEmpty()) {
                    Text("Days per week with $searchQuery", fontSize = 20.sp)

                    // Changed the height of line graph to be fixed so it doesn't get pushed off the screen by search bar
                    SymptomLineGraph(
                        data = weeklyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 20.dp)
                    )
                } else {
                    Text("You have not yet made an entry for \"$searchQuery\"", color = Color.Gray, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SymptomLineGraph(data: Map<LocalDate, Int>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(horizontal = 40.dp, vertical = 20.dp)) {
        val entries = data.toList()
        val canvasWidth = size.width
        val canvasHeight = size.height

        val xStep = if (entries.size > 1) canvasWidth / (entries.size - 1) else canvasWidth

        // get coordinates for the points
        val points = entries.mapIndexed { index, pair ->
            val x = index * xStep
            val yRatio = pair.second.toFloat() / 7 // Changed to float to avoid rounding errors
            val y = canvasHeight - (yRatio * canvasHeight)
            Offset(x, y)
        }

        // Draw grid lines
        for (i in 0..7) {
            val gridY = canvasHeight - (i.toFloat() / 7f * canvasHeight)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, gridY),
                end = Offset(canvasWidth, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line connecting points
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF6200EE),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw points and labels
        points.forEachIndexed { index, point ->
            drawCircle(Color(0xFF6200EE), radius = 6.dp.toPx(), center = point)

            drawIntoCanvas { canvas ->
                val dateStr = "${entries[index].first.monthValue}/${entries[index].first.dayOfMonth}"
                val textPaint = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(dateStr, point.x, canvasHeight + 40f, textPaint)

                // Show the actual count above the point
                canvas.nativeCanvas.drawText("${entries[index].second}", point.x, point.y - 20f, textPaint)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    allEntriesViewModel: AllJournalEntries = viewModel(),
    symptomsViewModel: SymptomsViewModel = viewModel()
) {
    val history = allEntriesViewModel.history.sortedBy { it.date }
    val improvements = mutableListOf<String>()

    symptomsViewModel.activities_list.forEach { activity ->

        symptomsViewModel.allSymptomsNames.forEach { sName ->
            // Change is a percentage. It is higher if there is a more significant difference in symptom before and after activity
            val change = symptomsViewModel.calculateImprovement(history, activity, sName)

            // Only report improvement if the reduction is large enough. Boundary in this case is 25%
            if (change != null && change >= 25.0) {
                improvements.add("Your $sName has decreased by ${change.toInt()}% since you started ${activity.name}.")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your Progress",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        // Mood line graph
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 0.dp, 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Spacer(modifier=Modifier.height(15.dp))
            Text(
                "Your Mood over Time",
                fontSize = 20.sp,
                modifier=Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier=Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(220.dp) // Manually resized to be much shorter
            ) {
                if (history.size < 2) {
                    Text("Need more entries!", modifier = Modifier.align(Alignment.Center))
                } else {
                    MoodChart(
                        entries = history,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card for showing which activities correlate to improvements in which symptoms
        if (improvements.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .absolutePadding(10.dp, 0.dp, 10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Activity Insights",
                    fontSize = 20.sp,
                    modifier=Modifier
                        .align(Alignment.CenterHorizontally)
                )
                improvements.forEach { text ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Text(text, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }

        SymptomGraphCard(history, symptomsViewModel)
    }
}


