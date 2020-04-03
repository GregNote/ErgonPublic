package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setColorFilterMultiply
import pl.garedgame.game.objects.Person
import pl.garedgame.game.util.doOnMainThread
import pl.garedgame.game.util.px

class AvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val skin =  ContextCompat.getDrawable(context, R.drawable.avatar_skin)?.constantState?.newDrawable()?.mutate()
    private val eyes = ContextCompat.getDrawable(context, R.drawable.avatar_eyes)?.constantState?.newDrawable()?.mutate()
    private val iris = ContextCompat.getDrawable(context, R.drawable.avatar_iris)?.constantState?.newDrawable()?.mutate()
    private val shirt = ContextCompat.getDrawable(context, R.drawable.avatar_shirt)?.constantState?.newDrawable()?.mutate()
    private val hair = ContextCompat.getDrawable(context, R.drawable.avatar_hair)?.constantState?.newDrawable()?.mutate()
    private val emptyColor = Color.parseColor("#bb000000")
    private var notEmpty = false

    init {
        updateColors(null)
    }

    fun updateColors(person: Person?) {
        doOnMainThread {
            notEmpty = person != null
            skin?.setColorFilterMultiply(person?.appearance?.skinTone ?: emptyColor)
            iris?.setColorFilterMultiply(person?.appearance?.irisColor ?: emptyColor)
            shirt?.setColorFilterMultiply(person?.appearance?.shirtColor ?: emptyColor)
            hair?.setColorFilterMultiply(person?.appearance?.hairColor ?: emptyColor)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            val margin = 1.px
            skin?.setBounds(margin, margin,width - margin, height - margin)
            skin?.draw(this)
            eyes?.setBounds(margin, margin, width - margin, height - margin)
            eyes?.draw(this)
            iris?.setBounds(margin, margin, width - margin, height - margin)
            iris?.draw(this)
            shirt?.setBounds(margin, margin, width - margin, height - margin)
            shirt?.draw(this)
            hair?.setBounds(margin, margin, width - margin, height - margin)
            hair?.draw(this)
        }
    }
}
