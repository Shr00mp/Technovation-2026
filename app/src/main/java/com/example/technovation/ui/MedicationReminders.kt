package com.example.technovation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.technovation.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class Medication(
    val id: Int,
    val name: String,
    val doseQuantity: Int,
    val doseUnit: String,
    val time: LocalTime
)

@RequiresApi(Build.VERSION_CODES.O)
class MedicationViewModel : ViewModel() {
    val allMedication = mutableStateListOf<Medication>()

    val completedTodayIds = mutableStateListOf<Int>()
    val completedTasks: List<Medication>
        get() = allMedication
            .filter { it.id in completedTodayIds }
            .sortedBy { it.time }

    val remainingTasks: List<Medication>
        get() = allMedication
            .filter { it.id !in completedTodayIds }
            .sortedBy { it.time }

    // For searching medications in the Manage page
    var searchQuery by mutableStateOf("")

    val filteredMedications: List<Medication>
        get() = allMedication.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.time }

    // Variables for storing the entries when saving / editing medication reminder
    var nameEntry by mutableStateOf("")
    var doseEntry by mutableIntStateOf(1)
    var doseUnitEntry by mutableStateOf("Pill")
    var timeEntry by mutableStateOf(LocalTime.of(8, 0))

    init {
        // Manually assigning IDs for the prototype
        allMedication.addAll(listOf(
            Medication(id = 1, name = "Lisinopril", doseQuantity = 1, doseUnit = "Pill", time = LocalTime.of(8, 0)),
            Medication(id = 2, name = "Vitamin D", doseQuantity = 2, doseUnit = "Capsules", time = LocalTime.of(12, 0)),
            Medication(id = 3, name = "Metformin", doseQuantity = 1, doseUnit = "Tablet", time = LocalTime.of(18, 0))
        ))
    }

    fun markAsDone(medication: Medication) {
        completedTodayIds.add(medication.id)
    }

    fun deleteMedication(medication: Medication) {
        allMedication.remove(medication)
        completedTodayIds.remove(medication.id)
    }

    fun loadMedicationForEdit(id: Int) {
        val med = allMedication.find { it.id == id }
        if (med != null) {
            // If the medication already exists, then we use OG values
            nameEntry = med.name
            doseEntry = med.doseQuantity
            doseUnitEntry = med.doseUnit
            timeEntry = med.time
        } else {
            // Is for when id = -1 to indicate make a new entry
            nameEntry = ""
            doseEntry = 1
            doseUnitEntry = "Pill"
            timeEntry = LocalTime.of(8, 0)
        }
    }

    fun saveMedication(id: Int) {
        if (id == -1) {
            // Create new ID by finding max and then +1 to it
            val newId = (allMedication.maxOfOrNull { it.id } ?: 0) + 1
            allMedication.add(Medication(
                id=newId,
                name=nameEntry,
                doseQuantity = doseEntry,
                doseUnit=doseUnitEntry,
                time=timeEntry))
        } else {
            // Update existing medication reminder
            val index = allMedication.indexOfFirst { it.id == id }
            if (index != -1) {
                allMedication[index] = Medication(
                    id=id,
                    name=nameEntry,
                    doseQuantity = doseEntry,
                    doseUnit=doseUnitEntry,
                    time=timeEntry
                )
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotifications(context: Context, medication: Medication) {
        // alarm manager is for handling device's internal clock
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // The things viewmodel sends to the broadcast receiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("MED_NAME", medication.name)
            putExtra("MED_AMOUNT", medication.doseQuantity)
            putExtra("MED_TYPE", medication.doseUnit)
        }

        // Intent only works when the app is awake and running
        // Pending intent used for when the app is closed and helps send the intent anyway
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id, // is unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // The calendar tells the system when exactly the notification is scheduled for
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, medication.time.hour)
            set(Calendar.MINUTE, medication.time.minute)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE,-1)
        }

        // If the time for the alarm today has already passed, then set notification for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicationTaskCard(
    modifier: Modifier = Modifier,
    currMedication: Medication,
    isCompleted: Boolean,
    onDoneClick: () -> Unit
) {
    // Users suggested that there should be more than two states for each task
    val currTime = LocalTime.now()
    val isOverdue = currTime.isAfter(currMedication.time) && !isCompleted
    val isUpcoming = currTime.isBefore(currMedication.time) &&
            currTime.plusMinutes(15).isAfter(currMedication.time) &&
            !isCompleted


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id= R.drawable.pill_icon),
                    contentDescription = "Medication Icon",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currMedication.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                    Text(
                        text = "${currMedication.doseQuantity} ${currMedication.doseUnit} | ${currMedication.time.format(
                            DateTimeFormatter.ofPattern("h:mm a"))}",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onDoneClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                enabled = !isCompleted,
                colors =
                    if (isCompleted) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else if (isOverdue) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else if (isUpcoming) {
                        ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Yellow, // Note to self: Yellow is slightly too bright here, do custom
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        )
                    }
                    else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
            ) {
                Text(
                    if (isCompleted) {"Done"}
                    else if (isOverdue) {"Mark as done (Overdue)"}
                    else if (isUpcoming) {"Mark as done (Upcoming)"}
                    else { "Mark as Done (Not Upcoming)"},
                    fontSize = 20.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MedicationViewModel = viewModel(),
    resourcesViewModel: ResourcesViewModel = viewModel())
{
    val scrollState = rememberScrollState()
    val remaining = viewModel.remainingTasks
    val completed = viewModel.completedTasks

    val allArticles by resourcesViewModel.articles.collectAsStateWithLifecycle()

    val medicationArticle = allArticles.find { it.title.equals("Managing your medication", ignoreCase = true)}

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = { navController.navigate(AppPages.ManageMedications.title)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Manage Medications", fontSize = 20.sp)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier=modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (medicationArticle != null) {
                ArticleCard(
                    article = medicationArticle,
                    onClick = { navController.navigate("detail/${medicationArticle.contentId}") },
                    onBookmarkClick = { resourcesViewModel.toggleSaved(medicationArticle) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Your Medication Reminders",
                fontSize = 30.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(35.dp))

            Text(
                "Tasks left for today",
                fontSize = 25.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(20.dp, top=15.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (remaining.isEmpty()) {
                Text("You have no more medication tasks for today!", modifier = Modifier.padding(8.dp))
            } else {
                remaining.forEach { medication ->
                    MedicationTaskCard(
                        currMedication = medication,
                        isCompleted = false,
                        onDoneClick = {viewModel.markAsDone(medication)})
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (!completed.isEmpty()) {
                Text(
                    "Already Completed Tasks Today",
                    fontSize = 25.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(0.dp, 15.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))
                completed.forEach { medication ->
                    MedicationTaskCard(
                        currMedication = medication,
                        isCompleted = true,
                        onDoneClick = {})
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ManageMedicationCard(
    medication: Medication,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill_icon),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medication.name, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Text(text = "${medication.doseQuantity} ${medication.doseUnit} | " +
                        "${medication.time.format(DateTimeFormatter.ofPattern("h:mm a"))} "
                    , fontSize=18.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
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
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ManageMedicationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MedicationViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val medsToShow = viewModel.filteredMedications

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Manage your Medication Reminders",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(35.dp))

        Button(
            onClick = {
                navController.navigate(AppPages.AddMedications.createRouteForAddingMedication(id=-1))
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Add New Medication Reminder", fontSize=20.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Search bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search your medication reminders") },
            placeholder = { Text("Type medication name...") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (medsToShow.isEmpty()) {
            Text("No medications found.", modifier = Modifier.padding(top = 20.dp))
        } else {
            medsToShow.forEach { medication ->
                ManageMedicationCard(
                    medication = medication,
                    onDelete = { viewModel.deleteMedication(medication) },
                    onEdit = {
                        navController.navigate(AppPages.AddMedications.createRouteForAddingMedication(id=medication.id))
                    }
                )
            }
        }
    }
}

@SuppressLint("ScheduleExactAlarm")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMedicationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MedicationViewModel = viewModel(),
    medicationId: Int = -1, // Is -1 to add and is actual medication id for edit
) {
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // When page starts we need to reload the medication for editing / adding
        LaunchedEffect(medicationId) {
            viewModel.loadMedicationForEdit(medicationId)
        }

        Text(
            text = if (medicationId == -1) "Add Medication" else "Edit Medication",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(55.dp))

        // Name field
        Text(
            text = "Enter the medication name",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = viewModel.nameEntry,
            onValueChange = { viewModel.nameEntry = it },
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Dosage stepper
        Text(
            text = "Enter dosage amount",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = { if (viewModel.doseEntry > 1) viewModel.doseEntry-- },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("-", fontSize = 30.sp)
                }

                Text(
                    text = viewModel.doseEntry.toString(),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 23.sp
                )

                FilledIconButton(
                    onClick = { viewModel.doseEntry++ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("+", fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Select dose type
        val options = listOf("Pill", "Tablet", "Capsule", "Syrup", "Other")
        var expanded by remember { mutableStateOf(false) }

        Text(
            text = "Select the dosage type",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(5.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = viewModel.doseUnitEntry,
                onValueChange = {},
                readOnly = true,
                label = { Text("Dosage Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            viewModel.doseUnitEntry = selection
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Time
        val context = androidx.compose.ui.platform.LocalContext.current
        Text(
            text = "Select the medication time",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text="Currently selected time: ${
                viewModel.timeEntry.format(
                    DateTimeFormatter.ofPattern(
                        "h:mm a"
                    )
                )
            }",
            fontSize = 17.sp,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedButton(
            onClick = {
                val timePicker = android.app.TimePickerDialog(
                    context,
                    { _, hour, minute -> viewModel.timeEntry = LocalTime.of(hour, minute) },
                    viewModel.timeEntry.hour,
                    viewModel.timeEntry.minute,
                    false
                )
                timePicker.show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Select New Time", fontSize=15.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Save button
        Button(
            onClick = {
                viewModel.saveMedication(medicationId)
                val currentMed = viewModel.allMedication.find { it.name == viewModel.nameEntry }
                currentMed?.let {
                    // will only run if currentMed actually exists since we are using ? and let
                    viewModel.scheduleNotifications(context, it)
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Save", fontSize=20.sp)
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context, p1: Intent?) {
        // This function is called when the broadcast receiver receives signal from system alarm
        // The intent is the things sent by the viewmodel and contains data about the medication

        val CHANNEL_ID = "medication_reminders"
        val medName = p1?.getStringExtra("MED_NAME")
        val medType = p1?.getStringExtra("MED_TYPE")
        val medAmount = p1?.getIntExtra("MED_AMOUNT", 0)

        // builds the channel for categorising notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Reminders" // Hardcoded
            val descriptionText = "Alarms for your medication schedule" // Hardcoded
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // This specifies what the notification will actually look like
        val builder = NotificationCompat.Builder(p0, CHANNEL_ID)
            .setSmallIcon(R.drawable.pill_icon)
            .setContentTitle("Medication Reminder")
            .setContentText("It's time to take your $medName medication. Take $medAmount $medType.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // For showing the notification
        // The next line is to do with getting the notification service on the actual device
        val notificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medName.hashCode(), builder.build())  // id has to be an int, so can't directly use medName
    }
}