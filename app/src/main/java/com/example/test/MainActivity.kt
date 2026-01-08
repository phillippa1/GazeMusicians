package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import camp.visual.eyedid.gazetracker.GazeTracker
import camp.visual.eyedid.gazetracker.callback.InitializationCallback
import camp.visual.eyedid.gazetracker.callback.TrackingCallback
import camp.visual.eyedid.gazetracker.callback.StatusCallback
import camp.visual.eyedid.gazetracker.callback.CalibrationCallback
import camp.visual.eyedid.gazetracker.constant.CalibrationModeType
import camp.visual.eyedid.gazetracker.constant.AccuracyCriteria
import com.example.test.gaze.GazeViewModel
import com.example.test.gaze.GestureViewModel
import com.example.test.interaction.InteractionMode
import com.example.test.interaction.InteractionViewModel
import com.example.test.ui.theme.TestTheme
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import camp.visual.eyedid.gazetracker.metrics.BlinkInfo
import camp.visual.eyedid.gazetracker.metrics.FaceInfo
import camp.visual.eyedid.gazetracker.metrics.GazeInfo
import camp.visual.eyedid.gazetracker.metrics.UserStatusInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import camp.visual.eyedid.gazetracker.constant.GazeTrackerOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_regular, FontWeight.SemiBold)
)

val songMap = mapOf(
    "Song 1" to 1,
    "Song 2" to 2,
    "Song 3" to 3,
    "Song 4" to 4,
    "Song 5" to 5
)

class MainActivity : ComponentActivity() {

    private lateinit var gazeViewModel: GazeViewModel
    private lateinit var gestureViewModel: GestureViewModel
    private var gazeTracker: GazeTracker? = null

    private val _calibrationPoint = mutableStateOf<Pair<Float, Float>?>(null)
    private val _calibrationProgress = mutableStateOf(0f)
    private val _isCalibrating = mutableStateOf(false)

    // Track face detection for recalibration
    private var faceMissingCount = 0
    private val faceMissingThreshold = 60  // ~2 seconds at 30fps
    private var needsRecalibration = false

    val calibrationPoint: State<Pair<Float, Float>?> = _calibrationPoint
    val calibrationProgress: State<Float> = _calibrationProgress
    val isCalibrating: State<Boolean> = _isCalibrating

    private val requestCameraPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initGazeTracker()
            } else {
                Log.w("MainActivity", "Camera permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TestTheme {
                gazeViewModel = viewModel()
                gestureViewModel = viewModel()
                val interactionViewModel: InteractionViewModel = viewModel()
                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "songList") {
                        composable("songList") {
                            SongListScreen(
                                navController = navController,
                                interactionViewModel = interactionViewModel,
                                gazeViewModel = gazeViewModel,
                                onRecalibrate = { triggerRecalibration() }
                            )
                        }

                        composable("song/{songNumber}") { backStackEntry ->
                            val songNumber =
                                backStackEntry.arguments?.getString("songNumber")?.toIntOrNull() ?: 1
                            MusicPage(navController, songNumber, interactionViewModel, gazeViewModel, gestureViewModel)
                        }
                    }

                    if (isCalibrating.value) {
                        CalibrationOverlay(
                            calibrationPoint = calibrationPoint.value,
                            progress = calibrationProgress.value
                        )
                    }
                }
            }
        }

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            initGazeTracker()
        }
    }

    private fun initGazeTracker() {
        val licenseKey = "dev_exzr35pwtjk5bd6effx07bg0kx6m6ibmai1w403u"

        // OPTIMIZED for straight-on phone use
        val options = GazeTrackerOptions.Builder()
            .setUseBlink(true)          // Enable blink detection (not used for gesture now, but keep enabled)
            .setUseGazeFilter(true)     // Smooths jittery gaze (helps with straight-on angle!)
            .setUseUserStatus(false)    // Disable user status (not needed)
            .build()

        GazeTracker.initGazeTracker(

            applicationContext,
            licenseKey,
            initializationCallback,
            options
        )
    }

    private val initializationCallback = InitializationCallback { tracker, error ->
        if (tracker != null) {
            gazeTracker = tracker
            gazeTracker?.setTrackingCallback(trackingCallback)
            gazeTracker?.setStatusCallback(statusCallback)
            gazeTracker?.setCalibrationCallback(calibrationCallback)
            gazeTracker?.startTracking()
            Log.d("Eyedid", "Init success, tracking started")
        } else {
            Log.e("Eyedid", "Initialization failed: $error")
        }
    }

    private val statusCallback = object : StatusCallback {
        override fun onStarted() {
            Log.d("Eyedid", "Tracking started, starting calibration...")

            val margin = 100f
            val display = windowManager.defaultDisplay
            val width = display.width.toFloat()
            val height = display.height.toFloat()

            gazeTracker?.startCalibration(
                CalibrationModeType.FIVE_POINT,  // Use 5-point calibration
                AccuracyCriteria.LOW,  // More forgiving for straight-on phone angle
                margin, margin, width - margin, height - margin
            )
        }

        override fun onStopped(errorType: camp.visual.eyedid.gazetracker.constant.StatusErrorType) {
            Log.d("Eyedid", "Tracking stopped: $errorType")
        }
    }

    private val calibrationCallback = object : CalibrationCallback {
        override fun onCalibrationProgress(progress: Float) {
            _calibrationProgress.value = progress
            Log.d("Eyedid", "Calibration progress: $progress")
        }

        override fun onCalibrationNextPoint(x: Float, y: Float) {
            _isCalibrating.value = true
            _calibrationPoint.value = Pair(x, y)
            Log.d("Eyedid", "Next calibration point: ($x, $y)")

            android.os.Handler(mainLooper).postDelayed({
                gazeTracker?.startCollectSamples()
            }, 1000)
        }

        override fun onCalibrationFinished(calibrationData: DoubleArray) {
            _isCalibrating.value = false
            _calibrationPoint.value = null
            Log.d("Eyedid", "Calibration finished successfully!")
        }

        override fun onCalibrationCanceled(calibrationData: DoubleArray?) {
            _isCalibrating.value = false
            _calibrationPoint.value = null
            Log.w("Eyedid", "Calibration was canceled")
        }
    }

    private val trackingCallback = object : TrackingCallback {
        override fun onMetrics(
            timestamp: Long,
            gazeInfo: GazeInfo,
            faceInfo: FaceInfo,
            blinkInfo: BlinkInfo,
            userStatusInfo: UserStatusInfo
        ) {
            gazeViewModel.updateGaze(gazeInfo.x, gazeInfo.y)

            // Track head tilts - pass roll (left/right tilt) from face info
            gestureViewModel.updateHeadPose(
                roll = faceInfo.roll,
                timestamp = timestamp
            )
        }

        override fun onDrop(timestamp: Long) {
            Log.w("Eyedid", "Tracking dropped")
        }
    }

    fun triggerRecalibration() {
        Log.d("MainActivity", "Recalibration triggered")
        val margin = 100f
        val display = windowManager.defaultDisplay
        val width = display.width.toFloat()
        val height = display.height.toFloat()

        gazeTracker?.stopCalibration()
        gazeTracker?.startCalibration(
            CalibrationModeType.FIVE_POINT,  // Use 5-point calibration
            AccuracyCriteria.LOW, // More forgiving for straight-on phone angle
            margin, margin, width - margin, height - margin
        )
    }
}

@Composable
fun CalibrationOverlay(
    calibrationPoint: Pair<Float, Float>?,
    progress: Float
) {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        if (calibrationPoint != null) {
            val xDp = with(density) { calibrationPoint.first.toDp() }
            val yDp = with(density) { calibrationPoint.second.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = xDp - 20.dp, y = yDp - 20.dp)
                    .size(40.dp)
                    .background(Color.Red, CircleShape)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Look at the red dot",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.width(200.dp),
                    color = Color.Red,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SongListScreen(
    navController: NavController,
    interactionViewModel: InteractionViewModel,
    gazeViewModel: GazeViewModel,
    onRecalibrate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InteractionModeSwitcher(interactionViewModel)
                SongList(navController)
                Spacer(modifier = Modifier.height(16.dp))
                RecalibrateButton(onRecalibrate)
            }
        }
        GazeDebugOverlay(gazeViewModel)
    }
}

@Composable
fun RecalibrateButton(onRecalibrate: () -> Unit) {
    OutlinedButton(
        onClick = onRecalibrate,
        modifier = Modifier
            .width(250.dp)
            .height(60.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, Color(0xFF1976D2)), // Blue border
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFE3F2FD), // Light blue background
            contentColor = Color(0xFF1976D2) // Blue text
        )
    ) {
        Text(
            text = "Recalibrate",
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
fun SongList(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        songMap.keys.forEach { songName ->
            SongButton(songName) {
                navController.navigate("song/${songMap[songName]}")
            }
        }
    }
}

@Composable
fun SongButton(songName: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(307.dp).height(98.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = songName,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )
        )
    }
}

@Composable
fun InteractionModeSwitcher(interactionViewModel: InteractionViewModel) {
    val currentMode = interactionViewModel.interactionMode

    // Horizontal view for four buttons
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        InteractionMode.entries.forEach { mode ->
            OutlinedButton(
                onClick = { interactionViewModel.updateInteractionMode(mode) },
                modifier = Modifier.height(45.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (mode == currentMode) Color.Black else Color.White,
                    contentColor = if (mode == currentMode) Color.White else Color.Black
                )
            ) {
                Text(
                    text = mode.name,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GazeDebugOverlay(gazeViewModel: GazeViewModel) {
    val gazePoint = gazeViewModel.gazePoint
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    if (gazePoint != null) {
        val screenWidthPx = windowInfo.containerSize.width
        val screenHeightPx = windowInfo.containerSize.height
        val screenWidthDp = with(density) { screenWidthPx.toDp().value }
        val screenHeightDp = with(density) { screenHeightPx.toDp().value }

        val scaledX = (gazePoint.x * screenWidthDp).coerceIn(0f, screenWidthDp)
        val scaledY = (gazePoint.y * screenHeightDp).coerceIn(0f, screenHeightDp)

        Box(
            modifier = Modifier
                .offset(x = scaledX.dp, y = scaledY.dp)
                .size(12.dp)
                .background(Color.Red, CircleShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    TestTheme {
        SongList(rememberNavController())
    }
}