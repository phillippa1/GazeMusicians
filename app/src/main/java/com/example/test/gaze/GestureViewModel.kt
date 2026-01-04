package com.example.test.gaze

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log

class GestureViewModel : ViewModel() {
    // Blink tracking for look + double blink
    private var lastBlinkTime = 0L
    private var blinkCount = 0
    private val doubleBlinkWindow = 1500L // 1.5 seconds window for double blink
    private var isLookingAtTarget = false
    private var currentTargetId: String? = null
    private var doubleBlinkCallback: ((String) -> Unit)? = null

    // Track previous blink state to detect COMPLETE blinks (start AND end)
    private var wasBlinking = false
    private var blinkStartTime = 0L
    private val minBlinkDuration = 50L // Minimum 50ms to count as real blink
    private val maxBlinkDuration = 500L // Maximum 500ms to count as blink (not just closed eyes)
    private val minTimeBetweenBlinks = 150L // Minimum time between blinks to avoid double-counting

    fun updateBlink(isBlinking: Boolean, timestamp: Long) {
        // Detect START of blink
        if (isBlinking && !wasBlinking) {
            blinkStartTime = timestamp
            wasBlinking = true
            Log.d("GestureViewModel", "Blink started at $timestamp")
            return
        }

        // Detect END of blink (this is when we count it!)
        if (!isBlinking && wasBlinking) {
            wasBlinking = false
            val blinkDuration = timestamp - blinkStartTime

            // Validate blink duration
            if (blinkDuration < minBlinkDuration) {
                Log.d("GestureViewModel", "Blink too short ($blinkDuration ms) - ignored")
                return
            }

            if (blinkDuration > maxBlinkDuration) {
                Log.d("GestureViewModel", "Eyes closed too long ($blinkDuration ms) - not a blink")
                return
            }

            Log.d("GestureViewModel", "✓ Valid blink detected! Duration: $blinkDuration ms")

            // Only process if looking at a target
            if (!isLookingAtTarget || currentTargetId == null) {
                Log.d("GestureViewModel", "Blink detected but not looking at target")
                return
            }

            processBlink(timestamp)
        }
    }

    private fun processBlink(timestamp: Long) {
        val timeSinceLastBlink = timestamp - lastBlinkTime

        // Ignore if too soon after last blink (prevents double-counting)
        if (lastBlinkTime > 0 && timeSinceLastBlink < minTimeBetweenBlinks) {
            Log.d("GestureViewModel", "Blink too soon after last ($timeSinceLastBlink ms) - ignored")
            return
        }

        // Check if this is the second blink
        if (blinkCount == 1 && timeSinceLastBlink < doubleBlinkWindow) {
            // Double blink detected!
            Log.d("GestureViewModel", "DOUBLE BLINK on $currentTargetId! (interval: $timeSinceLastBlink ms)")
            doubleBlinkCallback?.invoke(currentTargetId!!)
            blinkCount = 0
            lastBlinkTime = 0L
        } else {
            // First blink (or timeout - reset)
            blinkCount = 1
            lastBlinkTime = timestamp
            Log.d("GestureViewModel", "First blink on $currentTargetId - blink again within 1.5s!")
        }
    }

    fun setLookingAtTarget(targetId: String?, isLooking: Boolean) {
        // Only reset if actually changing target
        if (currentTargetId != targetId) {
            blinkCount = 0
            lastBlinkTime = 0L
            if (targetId != null) {
                Log.d("GestureViewModel", "Looking at: $targetId")
            } else {
                Log.d("GestureViewModel", "Stopped looking")
            }
        }

        isLookingAtTarget = isLooking
        currentTargetId = targetId

        if (!isLooking) {
            blinkCount = 0
            lastBlinkTime = 0L
        }
    }

    fun setDoubleBlinkCallback(callback: (String) -> Unit) {
        doubleBlinkCallback = callback
    }

    fun clearCallbacks() {
        doubleBlinkCallback = null
        isLookingAtTarget = false
        currentTargetId = null
        blinkCount = 0
        wasBlinking = false
        lastBlinkTime = 0L
        blinkStartTime = 0L
    }
}