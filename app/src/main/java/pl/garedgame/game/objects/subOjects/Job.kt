package pl.garedgame.game.objects.subOjects

import java.util.concurrent.TimeUnit

interface Job {
    fun progress(): Long
    fun duration(): Long
    fun update(pSinceMillis: Long)
    fun cost(): Cost

    fun timeRemain() = duration() - progress()
    fun isDone() = progress() == duration()
    fun progressPercent() = (progress().toDouble() / duration().toDouble() * 100.0).toInt()

    fun formattedRemainTime(): String {
        val millis = timeRemain()
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis))
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        return "$days days $hours:$minutes:$seconds"
    }
}