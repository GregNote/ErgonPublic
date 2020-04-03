package pl.garedgame.game.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import leakcanary.LeakSentry
import pl.garedgame.game.BuildConfig
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer

class RootActivity : AppCompatActivity() {

    private val callbacks = arrayListOf<Pair<Fragment, (() -> Unit)>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_root)
    }

    override fun onBackPressed() {
        for (callback in callbacks) {
            callback.second.invoke()
        }
        callbacks.clear()
        super.onBackPressed()
        SoundPlayer.instance.playSound(R.raw.sound_button_02)
    }

    fun registerOnBackPressedCallback(fragment: Fragment, callback: (() -> Unit)) {
        callbacks.add(fragment to callback)
    }

    fun unregisterOnBackPressedCallback(fragment: Fragment) {
        callbacks.remove(callbacks.find { it.first == fragment })
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.startMusic()
    }

    override fun onPause() {
        super.onPause()
        SoundPlayer.instance.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) LeakSentry.refWatcher.watch(this)
    }
}
