package com.example.test.interaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.InterFontFamily
import com.example.test.gaze.GestureViewModel
import android.util.Log
import androidx.compose.foundation.layout.height

@Composable
fun HeadTiltButton(
    text: String,
    buttonId: String,
    tiltDirection: GestureViewModel.TiltDirection,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gestureViewModel: GestureViewModel,
    interactionViewModel: InteractionViewModel,
    onClick: () -> Unit
) {
    var buttonBounds by remember { mutableStateOf<Rect?>(null) }
    val isDwelling = interactionViewModel.dwellingOn == buttonId
    val dwellProgress = interactionViewModel.dwellProgress

    // Using shared grace period from GestureViewModel now
    val isInGracePeriod = gestureViewModel.isInGracePeriod

    LaunchedEffect(interactionViewModel.interactionMode, enabled, buttonId, isInGracePeriod) {
        if (interactionViewModel.interactionMode == InteractionMode.HEAD_TILT && enabled && !isInGracePeriod) {
            Log.d("HeadTiltButton", "$buttonId registered for $tiltDirection tilts")

            gestureViewModel.registerDirectionalCallback(buttonId, tiltDirection) {
                Log.d("HeadTiltButton", "Head tilt $tiltDirection activated $buttonId")
                onClick()
            }
        } else {
            gestureViewModel.unregisterDirectionalCallback(buttonId)
            if (isInGracePeriod) {
                Log.d("HeadTiltButton", "$buttonId unregistered during grace period")
            }
        }
    }

    val isTilting = when (tiltDirection) {
        GestureViewModel.TiltDirection.LEFT -> gestureViewModel.isTiltingLeft
        GestureViewModel.TiltDirection.RIGHT -> gestureViewModel.isTiltingRight
    }

    val showAsActive = interactionViewModel.interactionMode == InteractionMode.HEAD_TILT && enabled && !isInGracePeriod

    val borderColor = when {
        isDwelling -> Color.Blue
        isInGracePeriod -> Color(0xFF9E9E9E) // Gray during grace period
        showAsActive && isTilting -> Color(0xFFFF6F00) // Bright orange when tilting!
        showAsActive -> Color(0xFFBDBDBD) // Light gray when waiting
        else -> Color.Black
    }

    val borderWidth = when {
        isDwelling -> 3.dp
        isInGracePeriod -> 2.dp
        showAsActive && isTilting -> 6.dp // Thicker when tilting
        showAsActive -> 2.dp
        else -> 1.dp
    }

    val backgroundColor = when {
        !enabled -> Color.Gray
        isInGracePeriod -> Color(0xFFE0E0E0) // Darker gray during grace period
        showAsActive && isTilting -> Color(0xFFFFCC80) // Light orange when tilting
        showAsActive -> Color(0xFFF5F5F5) // Very light gray when waiting
        else -> Color.White
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = {
                Log.d("HeadTiltButton", "Touch activated $buttonId")
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                )
                if (showAsActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tiltDirection == GestureViewModel.TiltDirection.LEFT) "◄" else "►",
                        style = TextStyle(
                            fontSize = 28.sp,
                            color = if (isTilting) Color(0xFFFF6F00) else Color.Gray
                        )
                    )
                }
            }
        }


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