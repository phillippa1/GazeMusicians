package com.example.test.gaze

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log
import kotlin.math.abs

class GestureViewModel : ViewModel() {
    // Head tilt tracking
    private var isLookingAtTarget = false
    private var currentTargetId: String? = null
    private var headTiltCallback: ((String) -> Unit)? = null

    // Track head tilt state
    private var baselineRoll: Float? = null
    private var tiltDetectionStartTime = 0L
    private var isTiltInProgress = false

    // Head tilt parameters - MUCH MORE SENSITIVE
    private val tiltThreshold = 8f       // Degrees - just 8° tilt (was 15°)
    private val minTiltDuration = 150L   // 150ms minimum hold (was 200ms)
    private val maxTiltDuration = 800L   // 800ms maximum (was 1000ms)
    private val cooldownPeriod = 400L    // 400ms cooldown (was 500ms)
    private var lastTiltTime = 0L

    // Baseline calibration
    private val baselineWindow = 30      // Track last 30 frames for baseline
    private val rollHistory = mutableListOf<Float>()

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

        // Only process tilts if looking at a target
        if (!isLookingAtTarget || currentTargetId == null || baselineRoll == null) {
            return
        }

        // Check for cooldown period
        if (timestamp - lastTiltTime < cooldownPeriod) {
            return
        }

        val tiltAmount = abs(roll - baselineRoll!!)

        // Detect tilt start
        if (tiltAmount >= tiltThreshold && !isTiltInProgress) {
            isTiltInProgress = true
            tiltDetectionStartTime = timestamp
            Log.d("GestureViewModel", "Head tilt started: ${String.format("%.1f", tiltAmount)}° from baseline")
        }

        // Detect tilt end (return to neutral)
        if (tiltAmount < tiltThreshold && isTiltInProgress) {
            val tiltDuration = timestamp - tiltDetectionStartTime
            isTiltInProgress = false

            if (tiltDuration >= minTiltDuration && tiltDuration <= maxTiltDuration) {
                Log.d("GestureViewModel", "✓ HEAD TILT on $currentTargetId! Duration: $tiltDuration ms, Angle: ${String.format("%.1f", tiltAmount)}°")
                headTiltCallback?.invoke(currentTargetId!!)
                lastTiltTime = timestamp
            } else {
                Log.d("GestureViewModel", "Head tilt invalid duration: $tiltDuration ms (need 150-800ms)")
            }
        }
    }

    fun setLookingAtTarget(targetId: String?, isLooking: Boolean) {
        if (currentTargetId != targetId) {
            if (targetId != null) {
                Log.d("GestureViewModel", "Looking at: $targetId - Slight head tilt (8°+) to activate!")
            } else {
                Log.d("GestureViewModel", "Stopped looking")
            }
        }

        isLookingAtTarget = isLooking
        currentTargetId = targetId

        if (!isLooking) {
            isTiltInProgress = false
            tiltDetectionStartTime = 0L
        }
    }

    fun setHeadTiltCallback(callback: (String) -> Unit) {
        headTiltCallback = callback
    }

    fun clearCallbacks() {
        headTiltCallback = null
        isLookingAtTarget = false
        currentTargetId = null
        isTiltInProgress = false
        tiltDetectionStartTime = 0L
        lastTiltTime = 0L
        baselineRoll = null
        rollHistory.clear()
    }
}