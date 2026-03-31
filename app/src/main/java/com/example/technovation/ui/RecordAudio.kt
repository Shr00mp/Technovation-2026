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
import androidx.core.content.ContextCompat
import java.time.LocalDate

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

//@RequiresApi(Build.VERSION_CODES.S)
//@Composable
//fun MakeRecording(
//    modifier: Modifier = Modifier,
//    navController: NavController
//) {
//    val context = LocalContext.current
//    var isRecording by remember { mutableStateOf(false) }
//    var secondsLeft by remember { mutableIntStateOf(5) }
//    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
//    var outputFile by remember { mutableStateOf("") }
//    val timerLength: Long = 5
//
//    val startRecording = {
//        val file = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
//            "recording_${LocalDate.now()}"
//        )
//        outputFile = file.absolutePath
//
//        recorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            prepare()
//            start()
//        }
//        isRecording = true
//    }
//
//    // Handle request for permission to use audio recording
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission(),
//    ) { isGranted ->
//        if (isGranted) {
//            startRecording()
//        }
//    }
//
//    // Timer
//    val timer = object: CountDownTimer(timerLength*1000, 1000) {
//        override fun onTick(millisUntilFunished: Long) {
//            secondsLeft = (millisUntilFunished / 1000).toInt() + 1 // rounding up
//        }
//
//        override fun onFinish() {
//            // Stop recording when 5 seconds are up
//            recorder?.stop()
//            recorder?.release()
//            recorder = null
//            isRecording = false
//            secondsLeft = 5
//        }
//    }
//
//    Column(
//        modifier=modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            "Make a Recording",
//            fontSize = 30.sp,
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .padding(0.dp, 15.dp)
//        )
//
//        Text(
//            "Take a deep breathe" +
//                    "\n When you are ready, click the Start Recording button below" +
//                    "\n Say 'aaaaa' steadily until the $timerLength-second timer runs out",
//            fontSize = 20.sp,
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        Button(
//            onClick = {
//                if (!isRecording) {
//                    if(ContextCompat.checkSelfPermission(context,
//                            Manifest.permission.RECORD_AUDIO)
//                        ==_root_ide_package_.android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                        timer.start()
//                        startRecording()
//                    } else {
//                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                    }
//                } else {
//                    recorder?.apply {
//                        stop()
//                        release()
//                    }
//                    recorder = null
//                    isRecording = false
//                }
//            },
//            enabled = !isRecording,
//            modifier = Modifier
//                .height(60.dp)
//                .width(350.dp),
//        ) {
//            Text(if (isRecording){"Recording..."} else {"Start Recording"}, fontSize=20.sp)
//        }
//    }
//}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MakeRecording(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var recorder: MediaRecorder? by remember {mutableStateOf(null)}
    var isRecording by remember {mutableStateOf(false)}
    var outputFile by remember {mutableStateOf("")}

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
            "Take a deep breathe" +
                    "\n When you are ready, click the Start Recording button below" +
                    "\n Say 'aaaaa' steadily until the 5-second timer runs out",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (!isRecording) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        "audio_recording_${LocalDate.now()}.mp3"
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
                }
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text(if (isRecording){"Recording..."} else {"Start Recording"}, fontSize=20.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (outputFile.isNotEmpty()) {
            Text("Output path: $outputFile")
        }
    }
}