package com.example.technovation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JournalPage(modifier: Modifier = Modifier) {
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
            onClick = {},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Make a new entry", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Button(
            onClick = {},
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

    // Is better to let the ViewModel handle the toggling
    fun toggleSymptom(id: Int, curr_list: MutableList<Symptom>) {
        val index = curr_list.indexOfFirst { it.id == id }
        var temp = curr_list[index]
        // need to replace the object with an entirely new copy
        curr_list[index] = temp.copy(selected = !temp.selected)
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
                .clickable(enabled=true, onClick = toggleSymptom)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = symptom.name,
            fontSize = 18.sp
        )
    }
}



@Composable
fun NewJournalEntry(
    modifier: Modifier = Modifier,
    viewModel: SymptomsViewModel = viewModel()) {
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
            "Your Journal",
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

            viewModel.physical_symptoms.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { viewModel.toggleSymptom(
                        symptom.id,
                        curr_list = viewModel.physical_symptoms)}
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

            viewModel.mental_symptoms.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { viewModel.toggleSymptom(
                        symptom.id,
                        curr_list = viewModel.mental_symptoms)}
                )
            }
        }

        Spacer(modifier=Modifier.height(30.dp))

        // Physical symptoms card
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

            viewModel.activities_list.forEach { symptom ->
                SymptomItem(
                    symptom = symptom,
                    toggleSymptom = { viewModel.toggleSymptom(
                        symptom.id,
                        curr_list = viewModel.activities_list)}
                )
            }
        }
    }
}
