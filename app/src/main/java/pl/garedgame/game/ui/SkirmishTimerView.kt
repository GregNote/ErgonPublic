package pl.garedgame.game.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setOnClick

class SkirmishTimerView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attributeSet, defStyleAttr) {

    enum class State {
        Normal,
        Slow,
        Pause
    }
    var animator: ValueAnimator? = null
    var state = State.Pause
        set(value) {
            field = value
            when (value) {
                State.Normal -> {
                    animator?.cancel()
                    animator = newAnimator(1f).apply { start() }
                }
                State.Slow -> {
                    animator?.cancel()
                    animator = newAnimator(0.05f).apply { start() }
                }
                State.Pause -> Game.instance.save.timer.state = 0f
            }
            invalidate()
        }

    init {
        setBackgroundResource(R.drawable.primary_framed)
        setOnClick {
            state = when (state) {
                State.Pause, State.Slow -> State.Normal
                State.Normal -> State.Slow
            }
        }
        state = State.Pause
    }

    private fun newAnimator(speed: Float): ValueAnimator {
        return ValueAnimator.ofFloat(Game.instance.save.timer.state, speed).apply {
            addUpdateListener {
                Game.instance.save.timer.state = it.animatedValue as Float
                invalidate()
            }
            duration = 360L
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            val id = when (state) {
                State.Normal -> android.R.drawable.ic_media_play
                State.Slow -> android.R.drawable.ic_media_rew
                State.Pause -> android.R.drawable.ic_media_pause
            }
            ContextCompat.getDrawable(context, id)?.also {
                it.bounds.set(0, 0, width, height)
                it.draw(this)
            }
        }
    }
}
