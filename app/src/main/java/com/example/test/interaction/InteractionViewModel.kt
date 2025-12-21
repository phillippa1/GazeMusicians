package com.example.test.interaction

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue


class InteractionViewModel : ViewModel() {

    var interactionMode by mutableStateOf(InteractionMode.TOUCH)
        private set

    fun updateInteractionMode(mode: InteractionMode) {
        interactionMode = mode
    }
}



