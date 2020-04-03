package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import pl.garedgame.game.Colors
import pl.garedgame.game.Hex
import pl.garedgame.game.objects.subOjects.Building

class BuildingView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attributeSet, defStyleAttr) {
    var getBuilding: (() -> Building?)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hex = Hex(0, 0, 0)
    private val buildingDrawer = BuildingDrawer()

    init {
        paint.style = Paint.Style.STROKE
        paint.color = Colors.colorPrimaryDark
    }

    fun setSelection(selected: Boolean) {
        paint.color =
                if (selected) Colors.lightBlue
                else Colors.colorPrimary
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildingDrawer.initialize(w, h, Math.min(w, h) / 2, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            getBuilding?.invoke()?.also {
                buildingDrawer.drawBuilding(it, hex, this, paint)
            }
        }
    }
}
