package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import pl.garedgame.game.Game
import java.util.concurrent.TimeUnit

data class Cost(
        @Expose val prefabricats: Long? = null,
        @Expose val platinum: Long? = null,
        @Expose val titanium: Long? = null,
        @Expose val ergon: Long? = null,
        @Expose val items: ArrayList<Pair<String, Int>>? = null,
        @Expose val time: Long? = null
) {

    private fun formattedTime(countMultiplier: Int): String {
        time?.apply {
            val millis = this * countMultiplier
            val days = TimeUnit.MILLISECONDS.toDays(millis)
            val hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis))
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            return "$days days $hours:$minutes:$seconds"
        }
        return ""
    }

    fun getString(countMultiplier: Int = 1): String {
        var cost = ""
        time?.apply { cost += "\n■ ${formattedTime(countMultiplier)}" }
        prefabricats?.apply { cost += "\n■ ${this * countMultiplier} prefabricats" }
        platinum?.apply { cost += "\n■ ${this * countMultiplier} platinum" }
        titanium?.apply { cost += "\n■ ${this * countMultiplier} titanium" }
        ergon?.apply { cost += "\n■ ${this * countMultiplier} ergon" }
        items?.apply {
            for (item in this) {
                val template = Game.content.templateEquipment(item.first)
                cost += "\n${item.second * countMultiplier} - ${template.name}"
            }
        }
        return cost
    }
}
