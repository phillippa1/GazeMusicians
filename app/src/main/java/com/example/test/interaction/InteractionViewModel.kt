package com.example.test.interaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class InteractionViewModel : ViewModel() {

    var interactionMode by mutableStateOf(InteractionMode.TOUCH)
        private set

    fun setInteractionMode(mode: InteractionMode) {
        interactionMode = mode
    }
}
