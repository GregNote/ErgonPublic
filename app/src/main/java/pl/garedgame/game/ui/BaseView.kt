package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import pl.garedgame.game.Colors
import pl.garedgame.game.Hex
import pl.garedgame.game.objects.subOjects.Building

class BaseView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attributeSet, defStyleAttr) {

    var getBuilding: ((Int) -> Building?)? = null
    var onClick: ((Int) -> Unit)? = null
    var onLongClick: ((Int) -> Unit)? = null
    var actionDownIndex = -1
    var actionDownTime = 0L

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hexs = arrayListOf<Hex>()
    private val buildingDrawer = BuildingDrawer()

    init {
        paint.style = Paint.Style.STROKE
        paint.color = Colors.colorPrimaryDark

        hexs.addAll(Hex.spiral(Hex(0, 0, 0), 2))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildingDrawer.initialize(w, h, Math.min(w, h) / 8, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (onClick != null || onLongClick != null) event?.apply {
            val index = hexs.indexOf(buildingDrawer.getHexByPoint(x, y))
            when (actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (index >= 0) {
                        actionDownIndex = index
                        actionDownTime = System.currentTimeMillis()
                    } else {
                        actionDownIndex = -1
                        return false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (index >= 0 && index == actionDownIndex) {
                        if (System.currentTimeMillis() - actionDownTime >= ViewConfiguration.getLongPressTimeout()) {
                            onLongClick?.invoke(index)
                        } else {
                            onClick?.invoke(index)
                        }
                    }
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            getBuilding?.also {
                for ((index, hex) in hexs.withIndex()) {
                    it.invoke(index).also { building ->
                        buildingDrawer.drawBuilding(building, hex, this, paint)
                    }
                }
            }
        }
    }
}
