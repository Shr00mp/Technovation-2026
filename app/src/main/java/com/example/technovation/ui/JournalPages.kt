package com.example.technovation.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties
import kotlinx.coroutines.delay
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
    LaunchedEffect(Unit) {
        allEntriesViewModel.initialize(symptomsViewModel)
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
                entry.physical_symptoms_entry.forEach { pastSymptom ->
                    symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.physical_symptoms)
                }
                entry.mental_symptoms_entry.forEach { pastSymptom ->
                    symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.mental_symptoms)
                }
                entry.activities_entry.forEach { pastSymptom ->
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Your Journal",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(55.dp))

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
        Symptom(2, "Tremors"),
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
    val symptomsAndActivityNames = (physical_symptoms + mental_symptoms + activities_list).map { it.name }

    // in order to allow user to edit their past entries, we need to store their mood, text and date in the viewmodel
    // so that it can be filled in correctly
    var tempMood by mutableStateOf(3)
    var tempText by mutableStateOf("")
    var pastEntryDate by mutableStateOf(LocalDate.now())

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
        tempMood = 3
        tempText = ""
        pastEntryDate = LocalDate.now()
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
        var thresholdDate = LocalDate.now()
        history.sortedBy { it.date }
        history.forEach { entry ->
            if (entry.activities_entry.contains(activity)) {
                thresholdDate = entry.date
            }
        }

        val beforeEntries = history.filter { it.date.isBefore(thresholdDate) }
        val afterEntries = history.filter { it.date.isAfter(thresholdDate) || it.date.isEqual(thresholdDate) }

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
        if (history.isEmpty()) return emptyMap()

        return history.groupBy { entry ->
            entry.date.with(DayOfWeek.MONDAY)
        }.mapValues { (_, entriesInWeek) ->
            entriesInWeek.count { entry ->
                entry.physical_symptoms_entry.any { it.name.equals(symptomName, ignoreCase = true) } ||
                        entry.mental_symptoms_entry.any { it.name.equals(symptomName, ignoreCase = true) } ||
                        entry.activities_entry.any {it.name.equals(symptomName, ignoreCase = true)}
            }
        }.toSortedMap()
    }

    fun loadValuesForEditing(entry: Entry) {
        pastEntryDate = entry.date
        tempMood = entry.mood
        tempText = entry.text_in_journal
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

        val voiceInputHandler = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if(activityResult.resultCode == Activity.RESULT_OK) {
                val resultText = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                resultText?.get(0)?.let{ recordedText ->
                    symptomsViewModel.tempText += recordedText
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
        // Title
        Text(
            "Make a New Entry",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(55.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodEmojis.forEachIndexed { index, emoji ->
                    val level = index + 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = emoji,
                            fontSize = 30.sp,
                            modifier = Modifier
                                .clickable { symptomsViewModel.tempMood = level }
                                .alpha(if (symptomsViewModel.tempMood == level) 1f else 0.3f)
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
                value=symptomsViewModel.tempText,
                onValueChange = { symptomsViewModel.tempText = it },
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
                    date = symptomsViewModel.pastEntryDate,
                    mood = symptomsViewModel.tempMood,
                    physical_symptoms_entry = symptomsViewModel.getSelectedPhysicalSymptoms(),
                    mental_symptoms_entry = symptomsViewModel.getSelectedMentalSymptoms(),
                    activities_entry = symptomsViewModel.getSelectedActivities(),
                    text_in_journal = symptomsViewModel.tempText
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

@RequiresApi(Build.VERSION_CODES.O)
fun addExampleData(
    physicalOptions: List<Symptom>,
    mentalOptions: List<Symptom>,
    activityOptions: List<Symptom>
): List<Entry> {
    val returnList = arrayListOf<Entry>()
    var currDate = LocalDate.now()
    var mood = 1
    for (i in 1..14) {
        currDate = currDate.minusDays(1)
        // For this example, we want the general trend for mood to be increasing over time
        if (i in 1..5) {mood =(3..5).random()}
        if (i in 6..9) {mood =(2..4).random()}
        if (i in 10..14) {mood =(1..3).random()}

        // We want the general trend for the first 2 physical symptoms and first 2 mental symptoms to be decreasing
        val currPhysical = mutableListOf<Symptom>()
        physicalOptions.forEachIndexed { index, symptom ->
            val randomChanceP = (1..100).random()
            if (index == 0 || index== 1) {
                val thresholdForChoosingSymptomP = i*5 // For i=1, would be 5% and goes up to 70% for i=14
                if (randomChanceP <= thresholdForChoosingSymptomP) {
                    currPhysical.add(symptom)
                }
            } else {
                if (randomChanceP <=20) {currPhysical.add(symptom)}
            }
        }

        val currMental = mutableListOf<Symptom>()
        mentalOptions.forEachIndexed { index, symptom ->
            val randomChangeM = (1..100).random()
            if (index == 0 || index== 1) {
                val thresholdForChoosingSymptomM = i*5
                if (randomChangeM <= thresholdForChoosingSymptomM){
                    currMental.add(symptom)
                }
            } else {
                if (randomChangeM <= 20) {currMental.add(symptom)}
            }
        }

        // User starts doing yoga and medication 11 and 8 days from now respectively
        val currActivities = mutableListOf<Symptom>()
        activityOptions.forEachIndexed { index, symptom ->
            val randomChanceA = (1..100).random()
            if (index == 0) {
                if (i <= 11) {
                    if (randomChanceA <= 80) {currActivities.add(symptom)}
                }
            } else if (index == 1) {
                if (i <= 8) {
                    if (randomChanceA <= 80) {
                        currActivities.add(symptom)
                    }
                }
            } else {
                if (randomChanceA < 10) {
                    currActivities.add(symptom)
                }
            }
        }

        returnList.add(
            Entry(
                date = currDate,
                mood = mood,
                physical_symptoms_entry = currPhysical,
                mental_symptoms_entry = currMental,
                activities_entry = currActivities,
                text_in_journal = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer " +
                        "sagittis tortor non massa varius, sit amet interdum nibh interdum. Duis " +
                        "hendrerit auctor sem, vel gravida dolor congue nec."
            )
        )
    }

    return returnList
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
class AllJournalEntries(): ViewModel() {
    val history = mutableStateListOf<Entry>()

    var initialised = false
    fun initialize(symptomsViewModel: SymptomsViewModel) {
        if (!initialised) {
            history.addAll(addExampleData(
                physicalOptions = symptomsViewModel.physical_symptoms,
                mentalOptions = symptomsViewModel.mental_symptoms,
                activityOptions = symptomsViewModel.activities_list
            ))
            initialised = true
        }
    }

    fun addEntry(entry: Entry) {
        // I have now added a function that allows the user to edit past entries
        // This means if a new entry is created every time, there is the risk of duplications
        val index = history.indexOfFirst { it.date == entry.date }
        if (index == -1) {
            history.add(0, entry)
        } else {
            history[index] = entry
        }
    }

    fun hasEntryForDay(dateToCheck: LocalDate): Boolean {
        for (entry in history) {
            if (entry.date == dateToCheck) {
                return true
            }
        }
        return false
    }

    fun deleteEntry(date: LocalDate) {
        for (entry in history) {
            if (entry.date == date) {
                history.remove(entry)
                break
            }
        }
    }

    fun updateEntry(date: LocalDate, updatedEntry: Entry) {
        for ((index, value) in history.withIndex()) {
            if (history[index].date == date) {
                history[index] = updatedEntry
                break
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastEntryCard(
    entry: Entry,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var dayOfWeek = entry.date.dayOfWeek
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
                "$dayOfWeek, $month $year"
            )

            Row {
                TextButton(onClick = onDelete) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
                    }
                }
                TextButton(onClick = onEdit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Edit", fontSize = 18.sp)
                    }
                }
            }

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
    symptomsViewModel: SymptomsViewModel,
    navController: NavController
) {
    Column(modifier=modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top)
    {
        Text(
            "All Past Entries",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(55.dp))

        if (allEntriesViewModel.history.isEmpty()) {
            Text("No entries yet. Start journaling!")
        } else {
            allEntriesViewModel.history.forEach { entry ->
                PastEntryCard(
                    entry,
                    onDelete = {allEntriesViewModel.deleteEntry(entry.date)},
                    onEdit = {
                        // Originally the symptoms are already toggled for this entry so we needed to reset it
                        // Otherwise old selections still apply when editing
                        symptomsViewModel.resetSelections()
                        symptomsViewModel.loadValuesForEditing(entry) // loads mood, text and date
                        // Date is so that when creating new entry, it is saved to the correct date ("Finish Entry" button)

                        // Users mentioned it was better to save previous selections instead of completely starting over
                        entry.physical_symptoms_entry.forEach { pastSymptom ->
                            symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.physical_symptoms)
                        }
                        entry.mental_symptoms_entry.forEach { pastSymptom ->
                            symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.mental_symptoms)
                        }
                        entry.activities_entry.forEach { pastSymptom ->
                            symptomsViewModel.toggleSymptom(pastSymptom.id, symptomsViewModel.activities_list)
                        }

                        navController.navigate(AppPages.NewEntry.title)
                    }
                    )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun AlreadyMadeEntryDialogue(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss },
        text = { Text("You have already made an entry today. \nYou may choose to edit your past entry.") },
        confirmButton = {
            Button(onClick = { onEdit() }) {
                Text("Edit result")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Close")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodChart(entries: List<Entry>, modifier: Modifier = Modifier) {
    val sortedEntries = entries.sortedBy { it.date }
    val moodValues = sortedEntries.map { it.mood.toDouble() }

    // Labelling the x-axis with only the start of every week
    // Tried labelling every day before but that got way too crowded
    val dateLabels = remember(sortedEntries) {
        sortedEntries.mapIndexed { index, entry ->
            if (entry.date.dayOfWeek == DayOfWeek.MONDAY) {
                "${entry.date.dayOfMonth} ${entry.date.month}"
            } else {
                ""
            }
        }
    }
    val moodEmojis = listOf("😢", "😟", "😐", "🙂", "😄")
    LineChart(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        data = remember(entries) {
            listOf(
                Line(
                    label = "Mood",
                    values = moodValues,
                    color = SolidColor(Color(0xFF23af92)),
                    firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                    secondGradientFillColor = Color.Transparent,
                    strokeAnimationSpec = tween(2000),
                    gradientAnimationDelay = 1000,
                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                )
            )
        },
        labelProperties = LabelProperties(
            enabled = true,
            labels = dateLabels,
            textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp),
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            contentBuilder = { value ->
                val index = value.toInt() - 1
                moodEmojis.getOrElse(index) { "" }
            }
        ),

        minValue = 1.0,
        maxValue = 5.0,
        animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SymptomGraphCard(history: List<Entry>, viewModel: SymptomsViewModel) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false)}
    // Was originally just symptomsNames, but it is better for user to be able to view activity progress too
    val allNames = viewModel.symptomsAndActivityNames
    val filteredResults = allNames.filter { it.contains(searchQuery, ignoreCase = true) }
    val weeklyData = viewModel.getWeeklySymptomCounts(history, searchQuery)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
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
            Spacer(modifier=Modifier.height(15.dp))

            // Search bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showResults = it.isNotEmpty() // Changed this so that results show as user types
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search for a symptom / activity") },
                    placeholder = { Text("Enter symptom / activity name") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search") }
                )

                // Search bar results
                if (showResults && filteredResults.isNotEmpty() && !allNames.contains(searchQuery)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            filteredResults.forEach { result ->
                                Text(
                                    text = result,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = result
                                            showResults = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // We changed graph to only show if search bar not expanded
            // This prevents the graph from being pushed off the screen
            if (searchQuery.isNotBlank() && allNames.contains(searchQuery)) {
                Spacer(modifier = Modifier.height(20.dp))

                if (weeklyData.isNotEmpty()) {
                    Text("Days per week with $searchQuery", fontSize = 20.sp,
                        modifier=Modifier
                            .align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(20.dp))
                    // Changed the height of line graph to be fixed so it doesn't get pushed off the screen by search bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SymptomBarChart(
                                data = weeklyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(bottom = 20.dp)
                            )
                            Text(
                                "Week of",
                                modifier=Modifier
                                    .align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 20.sp
                            )
                        }
                    }
                } else {
                    Text("You have not yet made an entry for \"$searchQuery\"", color = Color.Gray, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SymptomBarChart(data: Map<LocalDate, Int>, modifier: Modifier = Modifier) {
    // The data contains key as start of week date and int as the number occurrences that week
    val bars = arrayListOf<Bars>()

    data.forEach { (date, i) ->
        bars.add(
            Bars(
                label = "${date.dayOfMonth} ${date.month}",
                values = listOf(
                    Bars.Data(
                        label = "Count",
                        value = i.toDouble(),
                        color = SolidColor(Color(0xFF23af92))
                    )
                )
            )
        )
    }

    ColumnChart(
        modifier = modifier,
        data = bars,
        minValue = 0.0,
        maxValue = 7.0,
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            count = IndicatorCount.CountBased(8),
            contentBuilder = { value ->
                value.toInt().toString()
            }
        ),
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
            spacing = 1.dp,
            thickness = 40.dp
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),

    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    allEntriesViewModel: AllJournalEntries = viewModel(),
    symptomsViewModel: SymptomsViewModel = viewModel()
) {
    val history = allEntriesViewModel.history
    val improvementsMap = remember { mutableStateMapOf<Double, String>() }

    LaunchedEffect(Unit) {
        improvementsMap.clear()
        symptomsViewModel.activities_list.forEach { activity ->

            symptomsViewModel.allSymptomsNames.forEach { sName ->
                // Change is a percentage. It is higher if there is a more significant difference in symptom before and after activity
                val change = symptomsViewModel.calculateImprovement(history, activity, sName)

                // Only report improvement if the reduction is large enough. Boundary in this case is 25%
                if (change != null && change >= 25.0) {
                    improvementsMap[change] = "Your $sName symptom has decreased by ${change.toInt()}% since you started ${activity.name}."
                }
            }
        }
    }


    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Your Progress",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(15.dp))

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
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        SymptomGraphCard(history, symptomsViewModel)

        Spacer(modifier = Modifier.height(20.dp))


        // Card for showing which activities correlate to improvements in which symptoms
        if (improvementsMap.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .absolutePadding(10.dp, 0.dp, 10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    "Activity Insights",
                    fontSize = 20.sp,
                    modifier=Modifier
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier=Modifier.height(20.dp))
                improvementsMap.toSortedMap(compareByDescending { it }).values.take(5).forEach { text ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Text(text, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier=Modifier.height(5.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}


