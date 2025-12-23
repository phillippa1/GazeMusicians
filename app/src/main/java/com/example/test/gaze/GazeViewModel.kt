package com.example.test.gaze

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.ui.geometry.Offset

class GazeViewModel : ViewModel() {

    var gazePoint by mutableStateOf<Offset?>(null)
        private set

    fun updateGaze(x: Float, y: Float) {
        gazePoint = Offset(x, y)
    }
}
