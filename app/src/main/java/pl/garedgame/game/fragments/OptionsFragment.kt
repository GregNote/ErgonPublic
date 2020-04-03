package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_options.*
import pl.garedgame.game.*
import pl.garedgame.game.extensions.registerOnBackPressedCallback
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.extensions.unregisterOnBackPressedCallback
import pl.garedgame.game.multiplayer.NetUtils

class OptionsFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        musicVolumeSeekBar.progress = (musicVolumeSeekBar.max * Configuration.musicVolume).toInt()
        guiVolumeSeekBar.progress = (guiVolumeSeekBar.max * Configuration.guiVolume).toInt()
        effectVolumeSeekBar.progress = (effectVolumeSeekBar.max * Configuration.effectVolume).toInt()
        musicVolumeSeekBar.setOnSeekBarChangeListener(this)
        guiVolumeSeekBar.setOnSeekBarChangeListener(this)
        effectVolumeSeekBar.setOnSeekBarChangeListener(this)
        setDefault.setBounceOnClick {
            Configuration.setDefault()
            musicVolumeSeekBar.progress = (musicVolumeSeekBar.max * Configuration.musicVolume).toInt()
            guiVolumeSeekBar.progress = (guiVolumeSeekBar.max * Configuration.guiVolume).toInt()
            effectVolumeSeekBar.progress = (effectVolumeSeekBar.max * Configuration.effectVolume).toInt()
        }
        removeSavedGame.visibility = if (Game.instance.hasSavedGame()) View.VISIBLE else View.GONE
        removeSavedGame.setBounceOnClick { v ->
            Game.instance.clearSavedGame()
            v.visibility = if (Game.instance.hasSavedGame()) View.VISIBLE else View.GONE
        }
        version.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n${BuildConfig.GitHash}\n${NetUtils.getWifiIpAddress()}"
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            musicVolumeSeekBar -> {
                Configuration.musicVolume = progress / musicVolumeSeekBar.max.toFloat()
                SoundPlayer.instance.updateVolume()
            }
            guiVolumeSeekBar -> {
                Configuration.guiVolume = progress / guiVolumeSeekBar.max.toFloat()
                SoundPlayer.instance.playSound(R.raw.sound_console_01)
            }
            effectVolumeSeekBar -> {
                Configuration.effectVolume = progress / effectVolumeSeekBar.max.toFloat()
                SoundPlayer.instance.playSound(R.raw.pistol_shoot)
            }
        }
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.menu_theme)
        registerOnBackPressedCallback {
            Configuration.saveConfiguration()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterOnBackPressedCallback()
    }
}
