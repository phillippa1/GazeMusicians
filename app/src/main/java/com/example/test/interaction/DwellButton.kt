package com.example.test.interaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DwellButton(
    text: String,
    buttonId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gazeViewModel: GazeViewModel,
    interactionViewModel: InteractionViewModel,
    onClick: () -> Unit
) {
    var buttonBounds by remember { mutableStateOf<Rect?>(null) }
    val dwellProgress = interactionViewModel.dwellProgress
    val isDwelling = interactionViewModel.dwellingOn == buttonId

    val expansionPx = 150f
    val gazePoint = gazeViewModel.gazePoint
    val expandedBounds = remember(buttonBounds) {
        buttonBounds?.let { Rect(it.left - expansionPx, it.top - expansionPx, it.right + expansionPx, it.bottom + expansionPx) }
    }

    val isGazeInside = remember(gazePoint, expandedBounds) {
        gazePoint != null && expandedBounds != null && enabled &&
                expandedBounds.contains(Offset(gazePoint.x, gazePoint.y))
    }

    LaunchedEffect(isGazeInside) {
        interactionViewModel.setGazeOnButton(buttonId, isGazeInside)
        if (isGazeInside && enabled && interactionViewModel.interactionMode == InteractionMode.DWELL) {
            interactionViewModel.startOrResumeDwell(buttonId, onClick)
        }
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .matchParentSize()  // ← Changed from .width(160.dp).height(80.dp)
                .onGloballyPositioned { buttonBounds = it.boundsInWindow() },
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = if (isDwelling) 3.dp else 1.dp,
                color = if (isDwelling) Color.Blue else Color.Black
            ),
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
                    fontSize = 24.sp  // Increased from 20.sp for better visibility
                )
            )
        }

        if (isDwelling && dwellProgress > 0f) {
            CircularProgressIndicator(
                progress = dwellProgress,
                modifier = Modifier.align(Alignment.Center).width(50.dp).height(50.dp),
                color = Color.Blue,
                strokeWidth = 4.dp
            )
        }
    }
}