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

    val hasPrevious = currentPage > 1
    val hasNext = currentPage < MAX_PAGES_PER_SONG
    val currentMode = interactionViewModel.interactionMode

    DisposableEffect(Unit) {
        onDispose { gestureViewModel.clearCallbacks() }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---- Previous button at the top ----
        when (currentMode) {
            InteractionMode.PURSUITS -> {
                PursuitsButton(
                    text = "Previous",
                    buttonId = "previous",
                    direction = PursuitsDirection.UP,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage > 1) currentPage-- }
                )
            }
            InteractionMode.GESTURE -> {
                GestureButton(
                    text = "Previous",
                    buttonId = "previous",
                    gestureDirection = GestureDirection.LEFT,  // Not used but required
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    gestureViewModel = gestureViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage > 1) currentPage-- }
                )
            }
            else -> {
                DwellButton(
                    text = "Previous",
                    buttonId = "previous",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasPrevious,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage > 1) currentPage-- }
                )
            }
        }

        // ---- Music sheet placeholder with Back button centered ----
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.LightGray,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .width(220.dp)
                            .height(120.dp),
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
                                fontSize = 28.sp
                            )
                        )
                    }

                    Text(
                        text = "$songName - Page $currentPage of $MAX_PAGES_PER_SONG",
                        style = TextStyle(color = Color.Black, fontSize = 20.sp),
                        modifier = Modifier.padding(top = 140.dp)
                    )
                }
            }
        }

        // ---- Next button at the bottom ----
        when (currentMode) {
            InteractionMode.PURSUITS -> {
                PursuitsButton(
                    text = "Next",
                    buttonId = "next",
                    direction = PursuitsDirection.DOWN,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage < MAX_PAGES_PER_SONG) currentPage++ }
                )
            }
            InteractionMode.GESTURE -> {
                GestureButton(
                    text = "Next",
                    buttonId = "next",
                    gestureDirection = GestureDirection.RIGHT,  // Not used but required
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    gestureViewModel = gestureViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage < MAX_PAGES_PER_SONG) currentPage++ }
                )
            }
            else -> {
                DwellButton(
                    text = "Next",
                    buttonId = "next",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = hasNext,
                    gazeViewModel = gazeViewModel,
                    interactionViewModel = interactionViewModel,
                    onClick = { if (currentPage < MAX_PAGES_PER_SONG) currentPage++ }
                )
            }
        }
    }
}