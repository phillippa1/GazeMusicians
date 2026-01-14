package com.example.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

        // Previous button at the top now
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
                    gestureDirection = GestureDirection.LEFT,
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

        // Music sheet part
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            contentAlignment = Alignment.Center
        ) {
            // Music sheet for song one
            if (songNumber == 1) {
                val imageResource = when (currentPage) {
                    1 -> R.drawable.song1_page1
                    2 -> R.drawable.song1_page2
                    3 -> R.drawable.song1_page3
                    else -> R.drawable.song1_page1
                }

                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "Music sheet for $songName, page $currentPage",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                // For other songs, show placeholder for now
                Surface(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "$songName - Page $currentPage of $MAX_PAGES_PER_SONG",
                            style = TextStyle(color = Color.Black, fontSize = 20.sp)
                        )
                    }
                }
            }

            // Large invisible back button centered over the music
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.6f)  // 60% of screen width
                    .fillMaxHeight(0.5f), // 50% of center area height, might need to change this, see how it is when you use it
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.dp, Color.Transparent), // Invisible border
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent, // Invisible background
                    contentColor = Color.Transparent    // Invisible text
                )
            ) {
                Text(
                    text = "Back",
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        color = Color.Transparent // Invisible text
                    )
                )
            }
        }

        // Next button (at bottom)
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
                    gestureDirection = GestureDirection.RIGHT,
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