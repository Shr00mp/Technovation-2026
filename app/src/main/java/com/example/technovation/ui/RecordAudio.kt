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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import com.google.gson.annotations.SerializedName

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
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

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
            onClick = {},
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
    val severityScore: Double,

    @SerializedName("accuracy")
    val accuracy: Double,

    @SerializedName("top_indicators")
    val topIndicators: List<String>
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

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MakeRecording(
    modifier: Modifier = Modifier,
    navController: NavController
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
        if (hasFinishedRecording && !isRecording) {
            Text("If you are happy with your recording, click Get Results. \nIf not, you can make a new recording by clicking Record Again above.", fontSize=20.sp)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    if (isRecording)
                    if (outputFile.isNotEmpty()) {
                        isLoadingResults = true

                        uploadAudioToServer(outputFile) { result ->
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                analysisData = result
                                isLoadingResults = false
                            }
                        }
                    }
                },
                modifier = Modifier.height(60.dp).width(350.dp)
            ) {
                Text(if (isLoadingResults) "Processing..." else "Get Results", fontSize = 20.sp)
            }
        }
        analysisData?.let { result ->
            Spacer(modifier = Modifier.height(20.dp))
            ResultView(result) // I'll define this helper below
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
        Text("Results:", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("Severity: ${(result.severityScore * 100).toInt()}%")
        Text("Accuracy: ${(result.accuracy * 100).toInt()}%")

        Spacer(modifier = Modifier.height(10.dp))
        Text("Key Observations:", fontWeight = FontWeight.SemiBold)

        result.topIndicators.forEach { factor ->
            Text("• Your $factor is a primary factor.", fontSize = 16.sp)
        }
    }
}

class AllAudioResults : ViewModel() {
    val history = mutableStateListOf<AnalysisResult>()
    fun saveResult(result: AnalysisResult) {
        history.add(result)
    }
}
