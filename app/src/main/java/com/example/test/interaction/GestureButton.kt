package com.example.test.interaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.InterFontFamily
import com.example.test.gaze.GazeViewModel
import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.delay

@Composable
fun GestureButton(
    text: String,
    buttonId: String,
    gestureDirection: GestureDirection,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gazeViewModel: GazeViewModel,
    interactionViewModel: InteractionViewModel,
    onClick: () -> Unit
) {
    var buttonBounds by remember { mutableStateOf<Rect?>(null) }
    val gazePoint = gazeViewModel.gazePoint
    val isDwelling = interactionViewModel.dwellingOn == buttonId
    val dwellProgress = interactionViewModel.dwellProgress

    var lastTriggerTime by remember { mutableStateOf(0L) }
    val cooldownMs = 500L // 500ms cooldown between activations

    // Size of hitbox
    val expansionPx = 300f

    val expandedBounds = remember(buttonBounds) {
        buttonBounds?.let {
            Rect(
                left = it.left - expansionPx,
                top = it.top - expansionPx,
                right = it.right + expansionPx,
                bottom = it.bottom + expansionPx
            )
        }
    }

    // Check if gaze is inside bounds
    val isGazeInside = remember(gazePoint, expandedBounds, buttonBounds, interactionViewModel.interactionMode) {
        if (gazePoint == null || buttonBounds == null || !enabled) {
            false
        } else {
            val boundsToCheck = if (interactionViewModel.interactionMode == InteractionMode.GESTURE) {
                expandedBounds ?: buttonBounds!!
            } else {
                buttonBounds!!
            }
            boundsToCheck.contains(Offset(gazePoint.x, gazePoint.y))
        }
    }

    // Handle GESTURE mode - instant activation when you look at it
    LaunchedEffect(isGazeInside, interactionViewModel.interactionMode, enabled) {
        if (interactionViewModel.interactionMode == InteractionMode.GESTURE) {
            if (isGazeInside && enabled) {
                val currentTime = System.currentTimeMillis()
                val timeSinceLastTrigger = currentTime - lastTriggerTime

                if (timeSinceLastTrigger >= cooldownMs) {
                    Log.d("GestureButton", "Gaze instantly activated $buttonId")
                    lastTriggerTime = currentTime
                    onClick()

                    // Small delay to prevent retriggering if still looking
                    delay(100)
                } else {
                    Log.d("GestureButton", "Cooldown active for $buttonId, waiting ${cooldownMs - timeSinceLastTrigger}ms")
                }
            }
        }
    }

    // Visual feedback - just show if looking or not
    val showAsLooking = isGazeInside && interactionViewModel.interactionMode == InteractionMode.GESTURE

    val borderColor = when {
        isDwelling -> Color.Blue
        showAsLooking -> Color(0xFF4CAF50) // Green - looking
        else -> Color.Black
    }

    val borderWidth = when {
        isDwelling -> 3.dp
        showAsLooking -> 5.dp
        else -> 1.dp
    }

    val backgroundColor = when {
        !enabled -> Color.Gray
        showAsLooking -> Color(0xFFC8E6C9) // Light green
        else -> Color.White
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = {
                Log.d("GestureButton", "Touch activated $buttonId")
                onClick()
            },
            modifier = Modifier
                .matchParentSize()
                .onGloballyPositioned { coordinates ->
                    buttonBounds = coordinates.boundsInWindow()
                },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(width = borderWidth, color = borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = backgroundColor,
                contentColor = if (enabled) Color.Black else Color.DarkGray
            ),
            enabled = enabled
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            )
        }

        // Show circular progress indicator when dwelling (for other modes)
        if (isDwelling && dwellProgress > 0f) {
            CircularProgressIndicator(
                progress = dwellProgress,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(50.dp)
                    .height(50.dp),
                color = Color.Blue,
                strokeWidth = 4.dp
            )
        }
    }
}

enum class GestureDirection {
    LEFT,
    RIGHT
}