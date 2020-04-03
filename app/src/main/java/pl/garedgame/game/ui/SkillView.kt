package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import pl.garedgame.game.Colors
import pl.garedgame.game.objects.subOjects.Skill

class SkillView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }

    var active = false
        set(value) {
            field = value
            invalidate()
        }

    var skill = Skill()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            val step = width * 0.95f * 0.1f
            val size = width * 0.95f * 0.09f
            val lvl = if (isInEditMode) 5 else skill.getLevel()
            val progress = if (isInEditMode) 0.5f else skill.levelProgress()

            paint.color = if (active) Colors.gold else Colors.silver
            for (i in 0 until lvl) {
                drawRect(i * step, 0f, i * step + size, height.toFloat(), paint)
            }

            paint.color = Colors.colorPrimaryDark
            drawRect(lvl * step, 0f, lvl * step + size, height.toFloat(), paint)

            paint.color = Colors.silver
            drawRect(lvl * step, height.toFloat() * (1f - progress), lvl * step + size, height.toFloat(), paint)

            paint.color = Colors.colorPrimaryDark
            for (i in lvl+1 until 10) {
                drawRect(i * step, 0f, i * step + size, height.toFloat(), paint)
            }
        }
    }
}
