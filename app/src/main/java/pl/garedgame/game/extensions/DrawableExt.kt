package pl.garedgame.game.extensions

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build

@Suppress("DEPRECATION")
fun Drawable.setColorFilterMultiply(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
    } else {
        setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }
}