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
import com.example.test.interaction.InteractionViewModel
import com.example.test.interaction.InteractionMode

// ToDo- Obviously add the music sheets in and buy the book
// ToDo- Obviously get it working with eye tracking

// For now saying there are 3 pages per song
const val MAX_PAGES_PER_SONG = 3

@Composable
fun MusicPage(
    navController: NavController,
    songNumber: Int,
    interactionViewModel: InteractionViewModel
) {
    // Gets the song name from the map
    val songName = songMap.entries.find { it.value == songNumber }?.key ?: "Unknown Song"

    // State for the current page (starts at one for each song)
    var currentPage by remember { mutableStateOf(1) }

    // Calculate if previous and next are available
    val hasPrevious = currentPage > 1
    val hasNext = currentPage < MAX_PAGES_PER_SONG

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button on top left
        NavigationButton(
            text = "Back",
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            onClick = { navController.popBackStack() }
        )

        // Center - the music sheet box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)  // Slightly smaller to avoid button overlap
                .fillMaxHeight(0.75f),  // Slightly smaller to avoid button overlap
            contentAlignment = Alignment.Center
        ) {
            // Gray placeholder
            Surface(
                color = Color.LightGray,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Music Sheet for $songName - Page $currentPage\n(Gray Placeholder)",
                    style = TextStyle(color = Color.Black, fontSize = 20.sp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Bottom left: Previous button (made bigger)
        NavigationButton(
            text = "Previous",
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            enabled = hasPrevious,
            onClick = {
                when (interactionViewModel.interactionMode) {
                    InteractionMode.TOUCH -> {
                        if (hasPrevious) currentPage--
                    }

                    InteractionMode.DWELL -> {
                        // TEMPORARY: behave like touch
                        if (hasPrevious) currentPage--
                    }

                    InteractionMode.GESTURE -> {
                        // TEMPORARY: behave like touch
                        if (hasPrevious) currentPage--
                    }
                }
            }
        )

        // Bottom right: Next button (made bigger now)
        NavigationButton(
            text = "Next",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            enabled = hasNext,
            onClick = {
                when (interactionViewModel.interactionMode) {
                    InteractionMode.TOUCH -> {
                        if (hasNext) currentPage++
                    }

                    InteractionMode.DWELL -> {
                        // TEMPORARY: behave like touch
                        if (hasNext) currentPage++
                    }

                    InteractionMode.GESTURE -> {
                        // TEMPORARY: behave like touch
                        if (hasNext) currentPage++
                    }
                }
            }
        )
    }
}

@Composable
fun NavigationButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)  // Increased from 120.dp to 160.dp
            .height(80.dp),  // Increased from 60.dp to 80.dp
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.White else Color.Gray,
            contentColor = if (enabled) Color.Black else Color.DarkGray
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp  // Increased from 18.sp to 20.sp
            )
        )
    }
}