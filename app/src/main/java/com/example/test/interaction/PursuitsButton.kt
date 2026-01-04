package com.example.test.interaction

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.InterFontFamily
import com.example.test.gaze.GazeViewModel
import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

enum class PursuitsDirection {
    LEFT,  // Next button goes left
    RIGHT  // Previous button goes right
}

@Composable
fun PursuitsButton(
    text: String,
    buttonId: String,
    direction: PursuitsDirection,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gazeViewModel: GazeViewModel,
    interactionViewModel: InteractionViewModel,
    onClick: () -> Unit
) {
    var buttonBounds by remember { mutableStateOf<Rect?>(null) }
    val gazePoint = gazeViewModel.gazePoint
    var consecutiveGazeLoss by remember { mutableStateOf(0) }
    var trackingProgress by remember { mutableStateOf(0f) }
    var shouldReset by remember { mutableStateOf(false) }

    val isThisButtonPursuing = interactionViewModel.isPursuitActive(buttonId)

    val movementDistance = 400f
    val trackingDuration = 1000L
    val checkInterval = 50L
    val maxConsecutiveLoss = 4

    val animationProgress = remember { Animatable(0f) }
    val directionMultiplier = if (direction == PursuitsDirection.LEFT) -1f else 1f
    val expansionPx = 250f

    val expandedBounds = remember(buttonBounds, animationProgress.value) {
        buttonBounds?.let {
            val offsetX = animationProgress.value * movementDistance * directionMultiplier
            Rect(
                left = it.left + offsetX - expansionPx,
                top = it.top - expansionPx,
                right = it.right + offsetX + expansionPx,
                bottom = it.bottom + expansionPx
            )
        }
    }

    val isGazeInside = remember(gazePoint, expandedBounds) {
        if (gazePoint == null || expandedBounds == null || !enabled) {
            false
        } else {
            expandedBounds.contains(Offset(gazePoint.x, gazePoint.y))
        }
    }

    LaunchedEffect(isThisButtonPursuing, isGazeInside) {
        if (isThisButtonPursuing) {
            while (isThisButtonPursuing && trackingProgress < 1f) {
                delay(checkInterval)

                if (isGazeInside) {
                    trackingProgress += (checkInterval.toFloat() / trackingDuration)
                    consecutiveGazeLoss = 0
                    animationProgress.snapTo(trackingProgress.coerceIn(0f, 1f))
                } else {
                    consecutiveGazeLoss++
                    if (consecutiveGazeLoss >= maxConsecutiveLoss) {
                        shouldReset = true
                        break
                    }
                }

                if (trackingProgress >= 1f) {
                    onClick()
                    shouldReset = true
                    break
                }
            }

            if (shouldReset) {
                animationProgress.animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                interactionViewModel.clearActivePursuit()
                trackingProgress = 0f
                consecutiveGazeLoss = 0
                shouldReset = false
            }
        }
    }

    LaunchedEffect(isGazeInside, interactionViewModel.interactionMode, enabled, interactionViewModel.activePursuitButton) {
        when (interactionViewModel.interactionMode) {
            InteractionMode.DWELL -> {
                if (isThisButtonPursuing) {
                    animationProgress.snapTo(0f)
                    interactionViewModel.clearActivePursuit()
                    trackingProgress = 0f
                }
                if (isGazeInside && enabled) {
                    interactionViewModel.startDwell(buttonId, onClick)
                } else {
                    if (interactionViewModel.dwellingOn == buttonId) {
                        interactionViewModel.cancelDwell()
                    }
                }
            }
            InteractionMode.PURSUITS -> {
                if (enabled && isGazeInside && !isThisButtonPursuing && interactionViewModel.activePursuitButton == null) {
                    val started = interactionViewModel.startPursuit(buttonId)
                    if (started) {
                        trackingProgress = 0f
                        consecutiveGazeLoss = 0
                    }
                }
            }
            InteractionMode.TOUCH, InteractionMode.GESTURE -> {
                if (isThisButtonPursuing) {
                    animationProgress.snapTo(0f)
                    interactionViewModel.clearActivePursuit()
                    trackingProgress = 0f
                }
            }
        }
    }

    val borderColor = when {
        interactionViewModel.dwellingOn == buttonId -> Color.Blue
        interactionViewModel.interactionMode == InteractionMode.PURSUITS && isThisButtonPursuing -> Color.Magenta
        interactionViewModel.interactionMode == InteractionMode.PURSUITS && isGazeInside && interactionViewModel.activePursuitButton == null -> Color(0xFFFF69B4)
        else -> Color.Black
    }

    val borderWidth = when {
        interactionViewModel.dwellingOn == buttonId -> 3.dp
        interactionViewModel.interactionMode == InteractionMode.PURSUITS && (isThisButtonPursuing || (isGazeInside && interactionViewModel.activePursuitButton == null)) -> 4.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier.offset {
            IntOffset(
                x = (animationProgress.value * movementDistance * directionMultiplier).roundToInt(),
                y = 0
            )
        }
    ) {
        OutlinedButton(
            onClick = { onClick() },
            modifier = Modifier.width(160.dp).height(80.dp).onGloballyPositioned { buttonBounds = it.boundsInWindow() },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(borderWidth, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = when {
                    !enabled -> Color.Gray
                    interactionViewModel.interactionMode == InteractionMode.PURSUITS && isThisButtonPursuing -> Color(0xFFFCE4EC)
                    interactionViewModel.interactionMode == InteractionMode.PURSUITS && isGazeInside && interactionViewModel.activePursuitButton == null -> Color(0xFFF3E5F5)
                    else -> Color.White
                },
                contentColor = if (enabled) Color.Black else Color.DarkGray
            ),
            enabled = enabled
        ) {
            Text(text, style = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp))
        }
    }
}