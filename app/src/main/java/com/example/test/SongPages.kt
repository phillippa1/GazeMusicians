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

// ToDo- Make the next button and previous button bigger
// ToDo- move the next and prev button about so that you can see them better
// ToDo- Move the back button as well
// ToDo- Think about the music sheet size

// ToDo- Obviously add the music sheets in and buy the book
// ToDo- Obviously get it working with eyelure

// For now saying there are 3 pages per song
const val MAX_PAGES_PER_SONG = 3

@Composable
fun MusicPage(
    navController: NavController,
    songNumber: Int,
    interactionViewModel: InteractionViewModel // just pass it for now
) {
    // Gets the song name from the map
    val songName = songMap.entries.find { it.value == songNumber }?.key ?: "Unknown Song"

    // State for the current page (starts at one for each song)
    var currentPage by remember { mutableStateOf(1) }

    // Calculate is previous and next are available
    val hasPrevious = currentPage > 1
    val hasNext = currentPage < MAX_PAGES_PER_SONG

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button on top left
        NavigationButton(
            text = "Back",
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            onClick = { navController.popBackStack() }
        )

        // Center of the music the box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f),
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

        // Bottom left: Previous button
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


        // Bottom right: Next button
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
fun NavigationButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .width(120.dp)
            .height(60.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.White else Color.Gray,  // Gray out if disabled
            contentColor = if (enabled) Color.Black else Color.DarkGray
        ),
        enabled = enabled  // Disables interaction if false
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        )
    }
}
