package com.example.technovation.ui

import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.File
import android.Manifest
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

@Composable
fun AudioPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Audio Test",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(55.dp))

        Text(
            "Features of your speech such as pitch and frequency can tell you more about how severe your symptoms are.",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {navController.navigate(route = AppPages.MakeRecording.title)},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Take a new audio test", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Button(
            onClick = {navController.navigate(route = AppPages.PastRecordings.title)},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("See past test records", fontSize=20.sp)
        }
    }
}

data class AnalysisResult(
    @SerializedName("severity_score")
    val severityScore: Double = 0.0,
    @SerializedName("accuracy")
    val accuracy: Double = 0.0,
    @SerializedName("top_indicators")
    val topIndicators: List<String> = emptyList(),

    val date: String = System.currentTimeMillis().toString()
)

fun uploadAudioToServer(filePath: String, onResult: (AnalysisResult?) -> Unit) {
    val client = OkHttpClient()
    val file = File(filePath)

    // The key "file" must match the parameter name in your FastAPI function: save_audio(file: UploadFile)
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("https://unlanguid-ringlike-sheri.ngrok-free.dev/upload-audio/") // using ngrok
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace() // Handle connection errors here
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val jsonString = response.body?.string()
                // Need to convert the json response to AnalysisResult object
                val result = Gson().fromJson(jsonString, AnalysisResult::class.java)

                onResult(result) // Send the result back!
            } else {
                onResult(null)
            }
        }
    })
}

@Composable
fun MakeRecording(
    modifier: Modifier = Modifier,
    navController: NavController,
    allResultsViewmodel: AllAudioResults = viewModel()
) {
    var recorder: MediaRecorder? by remember {mutableStateOf(null)}
    var isRecording by remember {mutableStateOf(false)}
    var outputFile by remember {mutableStateOf("")}
    var hasFinishedRecording by remember {mutableStateOf(false)}
    var isLoadingResults by remember {mutableStateOf(false)}
    var analysisData by remember { mutableStateOf<AnalysisResult?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var hasGottenResults by remember { mutableStateOf(false) }

    var permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {granted ->}
    )

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Make a Recording",
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Text(
            "Take a deep breath." +
                    "\n When you are ready, click the Start Recording button below." +
                    "\n Say 'aaaaa' steadily for around 3 seconds." +
                    "\nClick the Stop Recording button when you are done.",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (!isRecording) {
                    analysisData = null
                    hasGottenResults = false

                    if (outputFile.isNotEmpty()) {
                        val oldFile = File(outputFile)
                        if (oldFile.exists()) {
                            oldFile.delete()
                        }
                    }

                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        "audio_recording_${System.currentTimeMillis()}.mp3"
                    )

                    outputFile = file.absolutePath

                    recorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(outputFile)
                        prepare()
                        start()
                    }

                    isRecording = true
                } else {
                    recorder?.apply {
                        stop()
                        release()
                    }

                    recorder = null
                    isRecording = false
                    hasFinishedRecording = true
                }
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text(if (isRecording){"Stop Recording"} else if (hasFinishedRecording) {"Record Again"} else {"Start Recording"}, fontSize=20.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
        if (hasFinishedRecording && !isRecording && !hasGottenResults) {
            Text("If you are happy with your recording, click Get Results. \nIf not, you can make a new recording by clicking Record Again above.", fontSize=20.sp)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    if (outputFile.isNotEmpty()) {
                        isLoadingResults = true

                        uploadAudioToServer(outputFile) { result ->
                            Handler(Looper.getMainLooper()).post {
                                analysisData = result
                                isLoadingResults = false

                                if (result != null) {
                                    showDialog = true
                                    hasGottenResults = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.height(60.dp).width(350.dp)
            ) {
                Text(if (isLoadingResults) "Processing..." else "Get Results", fontSize = 20.sp)
            }
        }
        // DIALOG POPUP
        if (showDialog && analysisData != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Analysis Results", fontWeight = FontWeight.Bold) },
                text = { ResultView(analysisData!!) },
                confirmButton = {
                    Button(onClick = {
                        allResultsViewmodel.saveResult(analysisData!!)
                        showDialog = false
                    }) {
                        Text("Save Result")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ResultView(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(16.dp)
    ) {
        Text("Severity: ${(result.severityScore * 100).toInt()}%", fontSize = 20.sp)
        Text("Accuracy: ${(result.accuracy * 100).toInt()}%", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(15.dp))

        result.topIndicators.forEach { factor ->
            Text("Your $factor is a primary factor.", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ResultsViewCard(result: AnalysisResult) {
    val timestamp = result.date.toLongOrNull() ?: System.currentTimeMillis()
    val dateObject = Date(timestamp)
    // EEEE = Full day name, MMMM = Full month name, yyyy = Year
    val formatter = SimpleDateFormat("EEEE, MMMM yyyy", Locale.getDefault())
    val formattedDate = formatter.format(dateObject)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 0.dp, 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = formattedDate,
        )
        Spacer(modifier = Modifier.height(20.dp))
        ResultView(result)
    }
}

class AllAudioResults : ViewModel() {
    val history = mutableStateListOf<AnalysisResult>()
    fun saveResult(result: AnalysisResult) {
        history.add(result)
    }
}

@Composable
fun AllPastRecordings(
    modifier: Modifier = Modifier,
    allAudioResults: AllAudioResults = viewModel(),
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
            "All Past Recording Results",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier=Modifier.height(55.dp))

        if (allAudioResults.history.isEmpty()) {
            Text("No recordings yet.")
        } else {
            allAudioResults.history.forEach { result ->
                ResultsViewCard(result)
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
