package pl.garedgame.game

import pl.garedgame.game.util.SharedPrefs

class Configuration {
    companion object {
        const val PERSON_HEALTH_BASE = 6
        const val PERSON_HEALTH_PER_LEVEL = 0.5f

        const val PERSON_SPEED_BASE = 0.0009f
        const val PERSON_SPEED_PER_LEVEL = 0.0001f

        const val PERSON_ROV_BASE = 2f
        const val PERSON_ROV_PER_LEVEL = 0.2f

        const val PERSON_FOV_BASE = 80f
        const val PERSON_FOV_PER_LEVEL = 8f

        const val minBehaviorTickGap: Long = 200L
        const val maxBehaviorTickGap: Long = 400L

        var musicVolume: Float = 0f
        var guiVolume: Float = 0f
        var effectVolume: Float = 0f

        init {
            setDefault()
        }

        fun setDefault() {
            val volume = if (BuildConfig.DEBUG) 0.1f else 0.5f
            musicVolume = volume
            guiVolume = volume
            effectVolume = volume
        }

        fun loadConfigurations() {
            SharedPrefs.doOnPrefs("CONFIGURATION_FILE_KEY") { sharedPref->
                musicVolume = sharedPref.getFloat("musicVolume", musicVolume)
                guiVolume = sharedPref.getFloat("guiVolume", guiVolume)
                effectVolume = sharedPref.getFloat("effectVolume", effectVolume)
            }
        }

        fun saveConfiguration() {
            SharedPrefs.doOnPrefs("CONFIGURATION_FILE_KEY") { sharedPref->
                sharedPref.edit().
                        putFloat("musicVolume", musicVolume).
                        putFloat("guiVolume", guiVolume).
                        putFloat("effectVolume", effectVolume).
                        apply()
            }
        }

        fun getBehaviorTickGap() = (minBehaviorTickGap..maxBehaviorTickGap).random()
    }
}
