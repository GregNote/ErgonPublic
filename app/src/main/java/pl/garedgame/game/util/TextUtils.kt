package pl.garedgame.game.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import pl.garedgame.game.Colors
import pl.garedgame.game.multiplayer.Command

class TextUtils {

    companion object {

        fun addLine(text: CharSequence, newLine: CharSequence): CharSequence {
            return SpannableStringBuilder().apply {
                if (text.isNotEmpty()) append(text).append("\n")
                append(newLine)
            }
        }

        fun createMessageSequence(message: Command.Message): CharSequence {
            val name = if (message.name.isEmpty()) "Server" else message.name
            return SpannableString("$name : ${message.msg}").apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, message.name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(Colors.colorPrimaryDark), 0, message.name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(BackgroundColorSpan(Colors.lightBlue), 0, message.name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
