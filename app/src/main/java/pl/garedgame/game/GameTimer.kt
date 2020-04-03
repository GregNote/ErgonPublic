package pl.garedgame.game

import com.google.gson.annotations.Expose
import kotlin.math.roundToLong

class GameTimer(
        @Expose var timestamp: Long = 2291792400000L
) {
    var state = 0f

    var onUpdate: ((Long) -> Unit)? = null

    fun onUpdate(millis: Long) {
        when(state) {
            in 0f..2f -> update(millis, 1)
            in 2f..16f -> update(millis, 2)
            in 16f..32f -> update(millis, 4)
            in 32f..Float.MAX_VALUE -> update(millis, 8)
        }
    }

    private fun update(millis: Long, count: Int) {
        val since = (millis * state).roundToLong() / count
        for (step in 0 until count) {
            onUpdate?.invoke(since)
            timestamp += since
        }
    }
}
