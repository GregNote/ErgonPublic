package pl.garedgame.game.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer

fun View.setOnClick(endCallback: ((View) -> Unit)? = null) {
    setOnClickListener {
        SoundPlayer.instance.playSound(R.raw.sound_button_02)
        endCallback?.invoke(this)
    }
}

fun View.setBounceOnClick(endCallback: ((View) -> Unit)? = null) {
    setOnClickListener {
        SoundPlayer.instance.playSound(R.raw.sound_button_02)
        animate().scaleX(0.95f).scaleY(0.95f).setDuration(35L).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(35L).withEndAction {
                endCallback?.invoke(this)
            }.start()
        }.start()
    }
}

fun View.setOnLongClick(endCallback: ((View) -> Boolean)? = null) {
    setOnLongClickListener {
        endCallback?.let {
            SoundPlayer.instance.playSound(R.raw.sound_button_02)
            it.invoke(this)
        } ?: false
    }
}

fun View.hideKeyboard(){
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}
