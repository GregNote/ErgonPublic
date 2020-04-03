package pl.garedgame.game

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build

class SoundPlayer private constructor() {
    companion object {
        val instance = SoundPlayer()
    }

    interface SoundVolume {
        fun getSoundVolume(): Float
    }
    enum class SoundType : SoundVolume {
        GUI {
            override fun getSoundVolume() = Configuration.guiVolume
        },
        EFFECT {
            override fun getSoundVolume() = Configuration.effectVolume
        }
    }

    data class Sound(val soundId: Int, val soundType: SoundType)

    private val sounds = mutableMapOf<Int, Sound>()
    private val musics = mutableMapOf<Int, MediaPlayer>()
    private var currentMusicId = -1

    @Suppress("DEPRECATION")
    private val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val sp21 = SoundPool.Builder()
        sp21.setMaxStreams(5)
        sp21.build()
    } else {
        SoundPool(5, AudioManager.STREAM_MUSIC, 0)
    }

    private fun loadSound(id: Int, soundType: SoundType) {
        if (!sounds.containsKey(id)) {
            sounds[id] = Sound(soundPool.load(GameApplication.instance, id, 1), soundType)
        }
    }

    private fun loadMusic(id: Int) {
        if (!musics.containsKey(id)) {
            musics[id] = MediaPlayer.create(GameApplication.instance, id).apply {
                isLooping = true
            }
        }
    }

    fun loadSounds() {
        loadMusic(R.raw.menu_theme)
        loadMusic(R.raw.main_theme)

        loadSound(R.raw.melee_attack, SoundType.EFFECT)
        loadSound(R.raw.pistol_shoot, SoundType.EFFECT)
        loadSound(R.raw.rifle_shoot, SoundType.EFFECT)
        loadSound(R.raw.shotgun_shoot, SoundType.EFFECT)
        loadSound(R.raw.sniper_shoot, SoundType.EFFECT)
        loadSound(R.raw.death_01, SoundType.EFFECT)
        loadSound(R.raw.death_02, SoundType.EFFECT)

        loadSound(R.raw.sound_button_01, SoundType.GUI)
        loadSound(R.raw.sound_button_02, SoundType.GUI)
        loadSound(R.raw.sound_console_01, SoundType.GUI)
        loadSound(R.raw.sound_console_02, SoundType.GUI)
        loadSound(R.raw.sound_error, SoundType.GUI)
    }

    fun updateVolume() {
        musics[currentMusicId]?.also {
            it.setVolume(Configuration.musicVolume, Configuration.musicVolume)
        }
    }

    fun playSound(id: Int) {
        sounds[id]?.apply {
            soundPool.play(soundId, soundType.getSoundVolume(), soundType.getSoundVolume(), 0, 0, 1f)
        }
    }

    fun playSound(name: String) {
        val ctx = GameApplication.instance
        val id = ctx.resources.getIdentifier(name, "raw", ctx.packageName)
        playSound(id)
    }

    fun playMusic(id: Int) {
        if (id != currentMusicId) {
            pauseMusic()
            startMusic(id)
        }
    }

    fun startMusic(id: Int = currentMusicId) {
        musics[id]?.also {
            it.setVolume(Configuration.musicVolume, Configuration.musicVolume)
            it.seekTo(0)
            it.start()
            currentMusicId = id
        }
    }

    fun pauseMusic(id: Int = currentMusicId) {
        musics[id]?.pause()
    }
}
