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

    // Map of buttonId to (direction, callback) for HEAD_TILT mode
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

    private var headTiltGracePeriodEnd = 0L
    val headTiltGracePeriodMs = 1000L // 1 second

    private val _isTiltingLeft = mutableStateOf(false)
    private val _isTiltingRight = mutableStateOf(false)
    val isTiltingLeft: Boolean get() = _isTiltingLeft.value
    val isTiltingRight: Boolean get() = _isTiltingRight.value

    private val _isInGracePeriod = mutableStateOf(false)
    val isInGracePeriod: Boolean get() = _isInGracePeriod.value

    enum class TiltDirection {
        LEFT,
        RIGHT
    }

    fun updateHeadPose(roll: Float, timestamp: Long) {
        _isInGracePeriod.value = timestamp < headTiltGracePeriodEnd

        rollHistory.add(roll)
        if (rollHistory.size > baselineWindow) {
            rollHistory.removeAt(0)
        }

        if (rollHistory.size >= 10) {
            baselineRoll = rollHistory.sorted()[rollHistory.size / 2]
        }

        if (baselineRoll == null) {
            return
        }

        if (timestamp - lastTiltTime < tiltCooldownPeriod) {
            return
        }

        if (timestamp < headTiltGracePeriodEnd) {
            return
        }

        val tiltAmount = roll - baselineRoll!!
        val absTiltAmount = abs(tiltAmount)

        _isTiltingLeft.value = tiltAmount > tiltThreshold
        _isTiltingRight.value = tiltAmount < -tiltThreshold

        // Tilt start
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

                if (isLookingAtTarget && currentTargetId != null) {
                    headTiltCallbackSimple?.invoke(currentTargetId!!)
                }

                directionalCallbacks.forEach { (buttonId, pair) ->
                    val (requiredDirection, callback) = pair
                    if (direction == requiredDirection) {
                        Log.d("GestureViewModel", "Calling callback for $buttonId")
                        callback()

                        headTiltGracePeriodEnd = timestamp + headTiltGracePeriodMs
                        Log.d("GestureViewModel", "Grace period started - all buttons inactive for ${headTiltGracePeriodMs}ms")
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

    fun setHeadTiltCallbackSimple(callback: (String) -> Unit) {
        headTiltCallbackSimple = callback
        directionalCallbacks.clear()
    }

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
        headTiltGracePeriodEnd = 0L
        _isInGracePeriod.value = false
    }
}