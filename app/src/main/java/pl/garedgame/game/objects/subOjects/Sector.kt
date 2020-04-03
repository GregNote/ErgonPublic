package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import pl.garedgame.game.util.Utils

class Sector(
        @Expose val x: Long,
        @Expose val y: Long,
        @Expose var informationLevel: InformationLevel
) {
    @Expose
    var platinum = arrayListOf<Int>()
    @Expose
    var titanium = arrayListOf<Int>()
    @Expose
    var ergon = arrayListOf<Int>()

    companion object {

        fun getCoordinate(value: Float): Long {
            return (value / 1000f).toLong() + if (value > 0f) 1L else -1L
        }

        fun random(x: Long, y: Long): Sector {
            val result = Sector(x, y, InformationLevel.Unknown)
            val pCount = getPlatinumResourceCount()
            while (result.platinum.size < pCount) {
                val pos = Utils.rand.nextInt(19)
                if (!result.platinum.contains(pos))
                    result.platinum.add(pos)
            }
            val tCount = getTitaniumResourceCount()
            while (result.titanium.size < tCount) {
                val pos = Utils.rand.nextInt(19)
                if (!result.titanium.contains(pos))
                    result.titanium.add(pos)
            }
            val eCount = getErgonResourceCount()
            while (result.ergon.size < eCount) {
                val pos = Utils.rand.nextInt(19)
                if (!result.ergon.contains(pos))
                    result.ergon.add(pos)
            }
            return result
        }
        private fun getPlatinumResourceCount(): Int {
            return when (Math.random()) {
                in 0.0..0.1 -> 3
                in 0.1..0.3 -> 2
                in 0.3..0.6 -> 1
                else -> 0
            }
        }
        private fun getTitaniumResourceCount(): Int {
            return when (Math.random()) {
                in 0.0..0.1 -> 2
                in 0.1..0.3 -> 1
                else -> 0
            }
        }
        private fun getErgonResourceCount(): Int {
            return when (Math.random()) {
                in 0.0..0.1 -> 1
                else -> 0
            }
        }
    }

    enum class InformationLevel {
        @SerializedName("Unknown") Unknown,
        @SerializedName("Scanned") Scanned
    }
}
