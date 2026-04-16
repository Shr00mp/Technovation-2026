package com.example.technovation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.technovation.R
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
            Medication(id = 1, name = "Lisinopril", doseQuantity = 1, doseUnit = "Pill", time = LocalTime.now().minusHours(1)),
            Medication(id = 2, name = "Vitamin D", doseQuantity = 2, doseUnit = "Capsules", time = LocalTime.now().plusMinutes(15)),
            Medication(id = 3, name = "Metformin", doseQuantity = 1, doseUnit = "Tablet", time = LocalTime.now().plusHours(1)),
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

    val cardBG = Color(0xFFDCE1DE)
    val dueSoon = Color(0xfff4e285)
    val overdue = Color(0xffbc4b51)
    val notDue = Color(0xff8cb369)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(cardBG)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pill_icon),
                    contentDescription = "Medication Icon",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currMedication.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${currMedication.doseQuantity} ${currMedication.doseUnit} | ${currMedication.time.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDoneClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                enabled = !isCompleted,
                colors = if (isCompleted) {
                    ButtonDefaults.buttonColors(
                        containerColor = notDue,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (isOverdue) {
                    ButtonDefaults.buttonColors(
                        containerColor = overdue,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else if (isUpcoming) {
                    ButtonDefaults.buttonColors(
                        containerColor = dueSoon,
                        contentColor = Color.Black
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = notDue,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            ) {
                Text(
                    text = if (isCompleted) "Done"
                    else if (isOverdue) "Mark as done (Overdue)"
                    else if (isUpcoming) "Mark as done (Upcoming)"
                    else "Mark as Done (Not Upcoming)",
                    fontSize = 18.sp
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
    resourcesViewModel: ResourcesViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val remaining = viewModel.remainingTasks
    val completed = viewModel.completedTasks
    val teaGreen = colorResource(id = R.color.tea_green)

    val allArticles by resourcesViewModel.articles.collectAsStateWithLifecycle()
    val medicationArticle = allArticles.find { it.title.equals("Managing your medication", ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = { navController.navigate(AppPages.ManageMedications.title) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff216869))
            ) {
                Text("Manage Medications", fontSize = 20.sp, color = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            if (medicationArticle != null) {
                Text(
                    text = "Important information:",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.absolutePadding(18.dp, 0.dp, 18.dp, 18.dp)
                )
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ArticleCard(
                        article = medicationArticle,
                        onClick = { navController.navigate("detail/${medicationArticle.contentId}") },
                        onBookmarkClick = { resourcesViewModel.toggleSaved(medicationArticle) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Your Medication Reminders",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = "Tasks left for today:",
                fontSize = 23.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp)
            )

            if (remaining.isEmpty()) {
                Text(
                    "You have no more medication tasks for today!",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                remaining.forEach { medication ->
                    MedicationTaskCard(
                        currMedication = medication,
                        isCompleted = false,
                        onDoneClick = { viewModel.markAsDone(medication) }
                    )
                }
            }

            if (completed.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Already Completed Today:",
                    fontSize = 23.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                completed.forEach { medication ->
                    MedicationTaskCard(
                        currMedication = medication,
                        isCompleted = true,
                        onDoneClick = {}
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
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
            .padding(vertical = 4.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color(0xffDCE1DE))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill_icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medication.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "${medication.doseQuantity} ${medication.doseUnit} | ${medication.time.format(DateTimeFormatter.ofPattern("h:mm a"))}")
            }

            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onDelete, contentPadding = PaddingValues(4.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onEdit, contentPadding = PaddingValues(4.dp)) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(25.dp),
                        tint = Color(0xff216869)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", color = Color(0xff216869))
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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "Manage Reminders",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { navController.navigate(AppPages.AddMedications.createRouteForAddingMedication(id = -1)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(Color(0xff216869))
        ) {
            Text("Add New Medication Reminder", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            Text("No medications found.", modifier = Modifier.padding(top = 20.dp).align(Alignment.CenterHorizontally))
        } else {
            medsToShow.forEach { medication ->
                ManageMedicationCard(
                    medication = medication,
                    onDelete = { viewModel.deleteMedication(medication) },
                    onEdit = { navController.navigate(AppPages.AddMedications.createRouteForAddingMedication(id = medication.id)) }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(medicationId) {
        // When page starts we need to reload the medication for editing / adding
        viewModel.loadMedicationForEdit(medicationId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = if (medicationId == -1) "Add Medication" else "Edit Medication",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name field
        Text("Enter the medication name", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.nameEntry,
            onValueChange = { viewModel.nameEntry = it },
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dosage stepper
        Text("Enter dosage amount", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = { if (viewModel.doseEntry > 1) viewModel.doseEntry-- },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.iconButtonColors(Color(0xff49A078))
            ) { Text("-", fontSize = 30.sp, color=Color.White) }

            Text(
                text = viewModel.doseEntry.toString(),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            FilledIconButton(
                onClick = { viewModel.doseEntry++ },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.iconButtonColors(Color(0xff49A078))
            ) { Text("+", fontSize = 24.sp, color=Color.White) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Select dose type
        val options = listOf("Pill", "Tablet", "Capsule", "Syrup", "Other")
        var expanded by remember { mutableStateOf(false) }

        Text("Select the dosage type", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
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

        Spacer(modifier = Modifier.height(24.dp))

        // Time
        Text("Select the medication time", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
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
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Selected: ${viewModel.timeEntry.format(DateTimeFormatter.ofPattern("h:mm a"))}")
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
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(Color(0xff216869))
        ) {
            Text("Save Medication", fontSize = 18.sp)
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
            .setAutoCancel(true)

        // For showing the notification
        // The next line is to do with getting the notification service on the actual device
        val notificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medName.hashCode(), builder.build())  // id has to be an int, so can't directly use medName
    }
}