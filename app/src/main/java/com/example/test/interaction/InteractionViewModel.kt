package com.example.test.interaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InteractionViewModel : ViewModel() {

    var interactionMode by mutableStateOf(InteractionMode.GESTURE)
        private set

    // Dwell state
    var dwellingOn by mutableStateOf<String?>(null)
        private set
    var dwellProgress by mutableStateOf(0f)
        private set
    private var dwellJob: Job? = null

    // Makes dwell duration accessible
    val dwellDurationMs: Long = 600L

    // Gesture state
    var gestureTriggered by mutableStateOf<String?>(null)
        private set

    // Track which button is currently being gazed at
    var currentGazeTarget by mutableStateOf<String?>(null)
        private set
    var currentGazeDirection by mutableStateOf<GestureDirection?>(null)
        private set


    var activePursuitButton by mutableStateOf<String?>(null)
        private set

    fun updateInteractionMode(mode: InteractionMode) {
        interactionMode = mode
        cancelDwell()
        clearGestureTrigger()
        clearGazeTarget()
        clearActivePursuit()
    }

    fun startDwell(buttonId: String, onComplete: () -> Unit) {
        if (dwellingOn == buttonId) return
        cancelDwell()
        dwellingOn = buttonId
        dwellProgress = 0f

        dwellJob = CoroutineScope(Dispatchers.Main).launch {
            val startTime = System.currentTimeMillis()
            try {
                while (dwellProgress < 1f) {
                    delay(16)
                }
                onComplete()
                cancelDwell()
            } catch (e: Exception) {
            }
        }
    }

    fun cancelDwell() {
        dwellJob?.cancel()
        dwellJob = null
        dwellingOn = null
        dwellProgress = 0f
    }

    fun updateDwellProgress(progress: Float) {
        dwellProgress = progress.coerceIn(0f, 1f)
    }

    fun setGazeTarget(buttonId: String, direction: GestureDirection) {
        currentGazeTarget = buttonId
        currentGazeDirection = direction
    }

    fun clearGazeTarget() {
        currentGazeTarget = null
        currentGazeDirection = null
    }

    fun triggerGesture(buttonId: String) {
        gestureTriggered = buttonId
    }

    fun clearGestureTrigger() {
        gestureTriggered = null
    }

    fun startPursuit(buttonId: String): Boolean {
        if (activePursuitButton == null) {
            activePursuitButton = buttonId
            return true
        }
        return false
    }

    fun startOrResumeDwell(buttonId: String, onComplete: () -> Unit) {
        if (dwellingOn != buttonId) {
            cancelDwell()
            dwellingOn = buttonId
            dwellProgress = 0f
        }

        dwellJob?.cancel()
        val gracePeriod = 2000L
        var lastGazeTime = System.currentTimeMillis()

        dwellJob = CoroutineScope(Dispatchers.Main).launch {
            val startTime = System.currentTimeMillis() - (dwellProgress * dwellDurationMs).toLong()

            while (true) {
                val now = System.currentTimeMillis()

                if (!isGazeOnButton(buttonId)) {
                    val elapsedSinceLeave = now - lastGazeTime
                    if (elapsedSinceLeave > gracePeriod) {
                        cancelDwell()
                        break
                    }
                } else {
                    lastGazeTime = now
                    dwellProgress = ((now - startTime).toFloat() / dwellDurationMs).coerceIn(0f, 1f)

                    if (dwellProgress >= 1f) {
                        onComplete()
                        cancelDwell()
                        break
                    }
                }
                delay(18)
            }
        }
    }

    private val gazeOnButtonMap = mutableMapOf<String, Boolean>()
    fun setGazeOnButton(buttonId: String, onButton: Boolean) {
        gazeOnButtonMap[buttonId] = onButton
    }

    fun isGazeOnButton(buttonId: String) = gazeOnButtonMap[buttonId] == true


    fun clearActivePursuit() {
        activePursuitButton = null
    }

    fun isPursuitActive(buttonId: String): Boolean {
        return activePursuitButton == buttonId
    }
}
