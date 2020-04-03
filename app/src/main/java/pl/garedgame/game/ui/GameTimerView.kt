package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import pl.garedgame.game.Colors
import pl.garedgame.game.Game
import pl.garedgame.game.R

class GameTimerView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attributeSet, defStyleAttr) {

    enum class Button {
        PlayPause,
        Speed1,
        Speed2,
        Speed3
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = Rect(0, 0, 0, 0)
    var currentMultiplierIndex = -1
    var timeMultipliers = arrayListOf(60f, 720f, 2880f)

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Colors.lightBlue
        setBackgroundResource(R.drawable.primary_framed)
    }

    fun setPause() {
        Game.instance.save.timer.state = 0f
        updateView()
    }

    fun setNormal() {
        Game.instance.save.timer.state = 1f
        updateView()
    }

    fun updateView() {
        currentMultiplierIndex = -1
        for ((index, value) in timeMultipliers.withIndex()) {
            if (value == Game.instance.save.timer.state) {
                currentMultiplierIndex = index
                break
            }
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    event.x in (width * 0.1665f)..(width * 0.8325f) && event.y in (height * 0f)..(height * 0.666f) -> {
                        return true
                    }
                    event.x in (width * 0f)..(width * 0.333f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        return true
                    }
                    event.x in (width * 0.333f)..(width * 0.666f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        return true
                    }
                    event.x in (width * 0.666f)..(width * 1f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> return true
            MotionEvent.ACTION_UP -> {
                when {
                    event.x in (width * 0.1665f)..(width * 0.8325f) && event.y in (height * 0f)..(height * 0.666f) -> {
                        changeTimerState(Button.PlayPause)
                        return true
                    }
                    event.x in (width * 0f)..(width * 0.333f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        changeTimerState(Button.Speed1)
                        return true
                    }
                    event.x in (width * 0.333f)..(width * 0.666f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        changeTimerState(Button.Speed2)
                        invalidate()
                        return true
                    }
                    event.x in (width * 0.666f)..(width * 1f) && event.y in (height * 0.666f)..(height * 1f) -> {
                        changeTimerState(Button.Speed3)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun changeTimerState(button: Button) {
        when (button) {
            Button.PlayPause -> {
                currentMultiplierIndex = -1
                if (Game.instance.save.timer.state != 0f) {
                    Game.instance.save.timer.state = 0f
                } else {
                    Game.instance.save.timer.state = 1f
                }
            }
            Button.Speed1, Button.Speed2, Button.Speed3 -> {
                val multiplierIndex = button.ordinal - 1
                if (Game.instance.save.timer.state != timeMultipliers[multiplierIndex]) {
                    currentMultiplierIndex = multiplierIndex
                    Game.instance.save.timer.state = timeMultipliers[multiplierIndex]
                } else {
                    currentMultiplierIndex = -1
                    Game.instance.save.timer.state = 1f
                }
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            rect.set((width * 0.1665).toInt(), 0, (width * 0.8325).toInt(), (height * 0.666).toInt())
            if (Game.instance.save.timer.state == 0f) drawRect(rect, paint)
            if (Game.instance.save.timer.state > 0f) {
                ContextCompat.getDrawable(context, android.R.drawable.ic_media_play)?.also {
                    it.bounds = rect
                    it.draw(this)
                }
            } else {
                ContextCompat.getDrawable(context, android.R.drawable.ic_media_pause)?.also {
                    it.bounds = rect
                    it.draw(this)
                }
            }
            rect.set(0, (height * 0.666).toInt(), (width * 0.333).toInt(), height)
            if (currentMultiplierIndex == 0) drawRect(rect, paint)
            ContextCompat.getDrawable(context, android.R.drawable.ic_media_ff)?.also {
                it.bounds = rect
                it.draw(this)
            }
            rect.set((width * 0.333).toInt(), (height * 0.666).toInt(), (width * 0.666).toInt(), height)
            if (currentMultiplierIndex == 1) drawRect(rect, paint)
            ContextCompat.getDrawable(context, android.R.drawable.ic_media_ff)?.also {
                it.bounds = rect
                it.draw(this)
            }
            rect.set((width * 0.666).toInt(), (height * 0.666).toInt(), width, height)
            if (currentMultiplierIndex == 2) drawRect(rect, paint)
            ContextCompat.getDrawable(context, android.R.drawable.ic_media_next)?.also {
                it.bounds = rect
                it.draw(this)
            }
        }
    }
}
