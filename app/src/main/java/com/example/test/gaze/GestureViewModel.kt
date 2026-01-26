package com.example.test.gaze

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log
import kotlin.math.abs

class GestureViewModel : ViewModel() {
    // Shared state
    private var isLookingAtTarget = false
    private var currentTargetId: String? = null

    // Head tilt tracking
    private var headTiltCallbackSimple: ((String) -> Unit)? = null

    // NEW: Map of buttonId to (direction, callback) for HEAD_TILT mode
    private val directionalCallbacks = mutableMapOf<String, Pair<TiltDirection, () -> Unit>>()

    private var baselineRoll: Float? = null
    private var tiltDetectionStartTime = 0L
    private var isTiltInProgress = false
    private val tiltThreshold = 8f
    private val minTiltDuration = 150L
    private val maxTiltDuration = 800L
    private val tiltCooldownPeriod = 400L
    private var lastTiltTime = 0L
    private val baselineWindow = 30
    private val rollHistory = mutableListOf<Float>()

    // Expose tilt state for UI
    private val _isTiltingLeft = mutableStateOf(false)
    private val _isTiltingRight = mutableStateOf(false)
    val isTiltingLeft: Boolean get() = _isTiltingLeft.value
    val isTiltingRight: Boolean get() = _isTiltingRight.value

    enum class TiltDirection {
        LEFT,
        RIGHT
    }

    fun updateHeadPose(roll: Float, timestamp: Long) {
        // Build baseline (neutral head position) from recent history
        rollHistory.add(roll)
        if (rollHistory.size > baselineWindow) {
            rollHistory.removeAt(0)
        }

        // Calculate baseline as median of recent rolls
        if (rollHistory.size >= 10) {
            baselineRoll = rollHistory.sorted()[rollHistory.size / 2]
        }

        if (baselineRoll == null) {
            return
        }

        // Check for cooldown period
        if (timestamp - lastTiltTime < tiltCooldownPeriod) {
            return
        }

        val tiltAmount = roll - baselineRoll!!
        val absTiltAmount = abs(tiltAmount)

        // Update UI state for visual feedback
        _isTiltingLeft.value = tiltAmount > tiltThreshold
        _isTiltingRight.value = tiltAmount < -tiltThreshold

        // Detect tilt start
        if (absTiltAmount >= tiltThreshold && !isTiltInProgress) {
            isTiltInProgress = true
            tiltDetectionStartTime = timestamp
            val direction = if (tiltAmount > 0) TiltDirection.LEFT else TiltDirection.RIGHT
            Log.d("GestureViewModel", "Head tilt started: ${String.format("%.1f", tiltAmount)}° (${direction})")
        }

        // Detect tilt end (return to neutral)
        if (absTiltAmount < tiltThreshold && isTiltInProgress) {
            val tiltDuration = timestamp - tiltDetectionStartTime
            isTiltInProgress = false

            if (tiltDuration >= minTiltDuration && tiltDuration <= maxTiltDuration) {
                val direction = if (tiltAmount > 0) TiltDirection.LEFT else TiltDirection.RIGHT
                Log.d("GestureViewModel", "✓ HEAD TILT detected! Direction: $direction")

                // Call simple callback for COMBINATION mode
                if (isLookingAtTarget && currentTargetId != null) {
                    headTiltCallbackSimple?.invoke(currentTargetId!!)
                }

                // Call directional callbacks for HEAD_TILT mode
                directionalCallbacks.forEach { (buttonId, pair) ->
                    val (requiredDirection, callback) = pair
                    if (direction == requiredDirection) {
                        Log.d("GestureViewModel", "Calling callback for $buttonId")
                        callback()
                    }
                }

                lastTiltTime = timestamp
            } else {
                Log.d("GestureViewModel", "Head tilt invalid duration: $tiltDuration ms (need 150-800ms)")
            }
        }
    }

    fun setLookingAtTarget(targetId: String?, isLooking: Boolean) {
        if (currentTargetId != targetId) {
            if (targetId != null) {
                Log.d("GestureViewModel", "Looking at: $targetId")
            } else {
                Log.d("GestureViewModel", "Stopped looking")
            }
        }

        isLookingAtTarget = isLooking
        currentTargetId = targetId

        if (!isLooking) {
            isTiltInProgress = false
            tiltDetectionStartTime = 0L
            _isTiltingLeft.value = false
            _isTiltingRight.value = false
        }
    }

    // For COMBINATION mode - any direction (original behavior)
    fun setHeadTiltCallbackSimple(callback: (String) -> Unit) {
        headTiltCallbackSimple = callback
        directionalCallbacks.clear()
    }

    // For HEAD_TILT mode - register button with its direction
    fun registerDirectionalCallback(buttonId: String, direction: TiltDirection, callback: () -> Unit) {
        directionalCallbacks[buttonId] = Pair(direction, callback)
        headTiltCallbackSimple = null
        Log.d("GestureViewModel", "Registered $buttonId for $direction tilts")
    }

    fun unregisterDirectionalCallback(buttonId: String) {
        directionalCallbacks.remove(buttonId)
    }

    fun clearCallbacks() {
        headTiltCallbackSimple = null
        directionalCallbacks.clear()
        isLookingAtTarget = false
        currentTargetId = null
        isTiltInProgress = false
        tiltDetectionStartTime = 0L
        lastTiltTime = 0L
        baselineRoll = null
        rollHistory.clear()
        _isTiltingLeft.value = false
        _isTiltingRight.value = false
    }
}