package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose

class Skill(@Expose var XP: Long = 0) {

    companion object {
        val levelArray = arrayListOf(50L, 100L, 250L, 500L, 1000L, 2500L, 5000L, 10000L, 25000L, 100000L)
        fun max(skill1: Skill, skill2: Skill): Skill {
            return if (skill1.XP > skill2.XP) skill1 else skill2
        }
    }

    fun getLevel(): Int {
        for ((index, value) in levelArray.withIndex()) {
            if (XP < value) {
                return index
            }
        }
        return levelArray.size
    }

    fun levelProgress(): Float {
        var prevXP = 0L
        for (value in levelArray) {
            if (XP < value) {
                return (XP - prevXP).toFloat() / (value - prevXP).toFloat()
            }
            prevXP = value
        }
        return 0f
    }

    fun use() {
        XP += 1
    }
}