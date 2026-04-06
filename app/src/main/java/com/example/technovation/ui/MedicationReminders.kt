package com.example.technovation.ui

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.technovation.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicationTaskCard(
    modifier: Modifier = Modifier,
    currMedication: Medication,
    isCompleted: Boolean,
    onDoneClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currMedication.doseQuantity} ${currMedication.doseUnit} • ${currMedication.time.format(
                            DateTimeFormatter.ofPattern("h:mm a"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Single "Done" Action
            Button(
                onClick = { onDoneClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                enabled = !isCompleted
            ) {
                Text(if (isCompleted) "Completed" else "Done")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MedicationViewModel = viewModel())
{
    val scrollState = rememberScrollState()
    val remaining = viewModel.remainingTasks
    val completed = viewModel.completedTasks

    Scaffold(
        // Bottom bar is anchored using scaffolding
        floatingActionButton = {
            Button(
                onClick = { navController.navigate(AppPages.ManageMedications.title)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Manage Medications", fontSize = 18.sp)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {paddingValues ->
        Column(
            modifier=modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Your Medication Reminders",
                fontSize = 30.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 15.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Tasks left for today",
                fontSize = 25.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 15.dp)
            )
            Spacer(modifier = Modifier.height(30.dp))
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
                Text(text = medication.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${medication.doseQuantity} ${medication.doseUnit}", style = MaterialTheme.typography.bodySmall)
                Text(text = medication.time.format(DateTimeFormatter.ofPattern("h:mm a")), style = MaterialTheme.typography.bodySmall)
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
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onEdit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Edit")
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
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Add New Medication Reminder", fontSize=20.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Search bar
        androidx.compose.material3.OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search your medication reminders") },
            placeholder = { Text("Type medication name...") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (medsToShow.isEmpty()) {
            Text("No medications found.", modifier = Modifier.padding(top = 20.dp))
        } else {
            medsToShow.forEach { medication ->
                ManageMedicationCard(
                    medication = medication,
                    onDelete = { viewModel.deleteMedication(medication) },
                    onEdit = {  }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMedicationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MedicationViewModel = viewModel()
) {

}