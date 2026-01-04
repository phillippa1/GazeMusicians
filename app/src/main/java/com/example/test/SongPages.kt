package com.example.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.gaze.GazeViewModel
import com.example.test.gaze.GestureViewModel
import com.example.test.interaction.GestureButton
import com.example.test.interaction.GestureDirection
import com.example.test.interaction.InteractionViewModel
import com.example.test.interaction.InteractionMode
import com.example.test.interaction.DwellButton
import com.example.test.interaction.PursuitsButton
import com.example.test.interaction.PursuitsDirection
import android.util.Log

const val MAX_PAGES_PER_SONG = 3

@Composable
fun MusicPage(
    navController: NavController,
    songNumber: Int,
    interactionViewModel: InteractionViewModel,
    gazeViewModel: GazeViewModel,
    gestureViewModel: GestureViewModel
) {
    val songName = songMap.entries.find { it.value == songNumber }?.key ?: "Unknown Song"
    var currentPage by remember { mutableStateOf(1) }

    // Ensure page is always within valid bounds
    if (currentPage < 1) {
        Log.w("MusicPage", "Page below minimum, resetting to 1")
        currentPage = 1
    }
    if (currentPage > MAX_PAGES_PER_SONG) {
        Log.w("MusicPage", "Page above maximum, resetting to $MAX_PAGES_PER_SONG")
        currentPage = MAX_PAGES_PER_SONG
    }

    val hasPrevious = currentPage > 1
    val hasNext = currentPage < MAX_PAGES_PER_SONG

    // Clean up callbacks when leaving the page
    DisposableEffect(Unit) {
        onDispose {
            gestureViewModel.clearCallbacks()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button - ALWAYS touch input
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .width(160.dp)
                .height(80.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Back",
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        }

        // Music sheet placeholder
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.LightGray,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "$songName - Page $currentPage of $MAX_PAGES_PER_SONG",
                    style = TextStyle(color = Color.Black, fontSize = 20.sp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Previous button - switches based on interaction mode
        when (interactionViewModel.interactionMode) {
            InteractionMode.GESTURE -> {
                GestureButton(
                    text = "Previous",
                    buttonId = "previous",
                    gestureDirection = GestureDirection.LEFT,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    gestureViewModel = gestureViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage > 1) {
                            Log.d("MusicPage", "Previous: $currentPage -> ${currentPage - 1}")
                            currentPage--
                        }
                    }
                )
            }
            InteractionMode.PURSUITS -> {
                // Previous button slides RIGHT (crosses over Next button)
                PursuitsButton(
                    text = "Previous",
                    buttonId = "previous",
                    direction = PursuitsDirection.RIGHT, // Slides right!
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage > 1) {
                            Log.d("MusicPage", "Previous: $currentPage -> ${currentPage - 1}")
                            currentPage--
                        }
                    }
                )
            }
            InteractionMode.DWELL, InteractionMode.TOUCH -> {
                DwellButton(
                    text = "Previous",
                    buttonId = "previous",
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage > 1) {
                            Log.d("MusicPage", "Previous: $currentPage -> ${currentPage - 1}")
                            currentPage--
                        }
                    }
                )
            }
        }

        // Next button - switches based on interaction mode
        when (interactionViewModel.interactionMode) {
            InteractionMode.GESTURE -> {
                GestureButton(
                    text = "Next",
                    buttonId = "next",
                    gestureDirection = GestureDirection.RIGHT,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    gestureViewModel = gestureViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage < MAX_PAGES_PER_SONG) {
                            Log.d("MusicPage", "Next: $currentPage -> ${currentPage + 1}")
                            currentPage++
                        }
                    }
                )
            }
            InteractionMode.PURSUITS -> {
                // Next button slides LEFT (crosses over Previous button)
                PursuitsButton(
                    text = "Next",
                    buttonId = "next",
                    direction = PursuitsDirection.LEFT, // Slides left!
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage < MAX_PAGES_PER_SONG) {
                            Log.d("MusicPage", "Next: $currentPage -> ${currentPage + 1}")
                            currentPage++
                        }
                    }
                )
            }
            InteractionMode.DWELL, InteractionMode.TOUCH -> {
                DwellButton(
                    text = "Next",
                    buttonId = "next",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = {
                        if (currentPage < MAX_PAGES_PER_SONG) {
                            Log.d("MusicPage", "Next: $currentPage -> ${currentPage + 1}")
                            currentPage++
                        }
                    }
                )
            }
        }
    }
}