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
import com.example.test.gaze.GestureViewModel
import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width

@Composable
fun GestureButton(
    text: String,
    buttonId: String,
    gestureDirection: GestureDirection,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gazeViewModel: GazeViewModel,
    gestureViewModel: GestureViewModel,
    interactionViewModel: InteractionViewModel,
    onClick: () -> Unit
) {
    var buttonBounds by remember { mutableStateOf<Rect?>(null) }
    val gazePoint = gazeViewModel.gazePoint
    val isDwelling = interactionViewModel.dwellingOn == buttonId
    val dwellProgress = interactionViewModel.dwellProgress

    var isLookingAtThis by remember { mutableStateOf(false) }

    // Large hitbox for easy targeting with full-width buttons
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

    // Handle different interaction modes
    LaunchedEffect(isGazeInside, interactionViewModel.interactionMode) {
        val wasLookingAtThis = isLookingAtThis
        isLookingAtThis = isGazeInside

        when (interactionViewModel.interactionMode) {
            InteractionMode.DWELL -> {
                if (isGazeInside && enabled) {
                    interactionViewModel.startDwell(buttonId, onClick)
                } else {
                    if (interactionViewModel.dwellingOn == buttonId) {
                        interactionViewModel.cancelDwell()
                    }
                }
            }
            InteractionMode.GESTURE -> {
                // Tell GestureViewModel we're looking at this button
                if (isGazeInside && enabled) {
                    if (!wasLookingAtThis) {
                        Log.d("GestureButton", "Started looking at $buttonId - Tilt head to activate!")
                    }
                    gestureViewModel.setLookingAtTarget(buttonId, true)

                    // Setup head tilt callback
                    gestureViewModel.setHeadTiltCallback { targetId ->
                        if (targetId == buttonId) {
                            Log.d("GestureButton", "Head tilt activated $buttonId")
                            onClick()
                        }
                    }
                } else {
                    if (wasLookingAtThis) {
                        Log.d("GestureButton", "Stopped looking at $buttonId")
                        gestureViewModel.setLookingAtTarget(null, false)
                    }
                }
            }
            InteractionMode.TOUCH -> {
                // Touch handled by onClick below
            }
            InteractionMode.PURSUITS -> {
                // Pursuits handled by PursuitsButton
            }
        }
    }

    // Visual feedback
    val showAsLooking = isGazeInside && interactionViewModel.interactionMode == InteractionMode.GESTURE

    val borderColor = when {
        isDwelling -> Color.Blue
        showAsLooking -> Color(0xFFFF9800) // Orange - ready for head tilt
        else -> Color.Black
    }

    val borderWidth = when {
        isDwelling -> 3.dp
        showAsLooking -> 5.dp
        else -> 1.dp
    }

    val backgroundColor = when {
        !enabled -> Color.Gray
        showAsLooking -> Color(0xFFFFE0B2) // Light orange
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

        // Show circular progress indicator when dwelling
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