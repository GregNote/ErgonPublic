package pl.garedgame.game

import android.graphics.Color
import androidx.core.content.ContextCompat

class Colors {
    companion object {
        val lineGreen = Color.rgb(31, 95, 31)
        val scannedGreen = Color.rgb(7, 31, 7)
        val lightBlue = ContextCompat.getColor(GameApplication.instance.baseContext, R.color.lightBlue)
        val colorPrimary = ContextCompat.getColor(GameApplication.instance.baseContext, R.color.colorPrimary)
        val colorPrimaryDark = ContextCompat.getColor(GameApplication.instance.baseContext, R.color.colorPrimaryDark)
        val gold = ContextCompat.getColor(GameApplication.instance.baseContext, R.color.gold)
        val silver = ContextCompat.getColor(GameApplication.instance.baseContext, R.color.silver)
    }
}