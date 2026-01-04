package com.example.test.gaze

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.graphics.PointF
import android.util.Log

class GazeViewModel : ViewModel() {
    var gazePoint by mutableStateOf<PointF?>(null)
        private set

    private var frameCount = 0

    fun updateGaze(x: Float, y: Float) {
        // Use RAW coordinates for now (no filter)
        gazePoint = PointF(x, y)

        // Debug logging every 30 frames
        frameCount++
        if (frameCount % 30 == 0) {
            Log.d("GazeViewModel", "Gaze updated: ($x, $y)")
        }
    }
}