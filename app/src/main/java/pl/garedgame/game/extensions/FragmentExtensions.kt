package pl.garedgame.game.extensions

import androidx.fragment.app.Fragment
import pl.garedgame.game.activities.RootActivity

fun Fragment.registerOnBackPressedCallback(callback: (() -> Unit)) {
    val rootActivity = requireActivity() as RootActivity
    rootActivity.registerOnBackPressedCallback(this, callback)
}

fun Fragment.unregisterOnBackPressedCallback() {
    val rootActivity = requireActivity() as RootActivity
    rootActivity.unregisterOnBackPressedCallback(this)
}
