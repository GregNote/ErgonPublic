package pl.garedgame.game.fragments

import android.content.pm.ActivityInfo
import android.view.WindowManager
import leakcanary.LeakSentry
import pl.garedgame.game.BuildConfig

open class Fragment(private val isPortrait: Boolean = false) : androidx.fragment.app.Fragment() {

    override fun onResume() {
        super.onResume()
        updateOrientation()
    }

    private fun updateOrientation() {
        activity?.let {
            if (isPortrait) {
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } else {
                it.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) LeakSentry.refWatcher.watch(this)
    }
}
