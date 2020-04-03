package pl.garedgame.game.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import pl.garedgame.game.Colors
import pl.garedgame.game.GameApplication
import pl.garedgame.game.Hex
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setColorFilterMultiply
import pl.garedgame.game.objects.subOjects.Building

class BuildingDrawer {
    private val rect = Rect()
    private lateinit var hexLayout: Hex.Layout
    private var width = 0f
    private var height = 0f

    private val hexTile = (ContextCompat.getDrawable(GameApplication.instance, R.drawable.hex_tile) as Drawable).apply {
        setColorFilterMultiply(Colors.colorPrimary)
    }
    private val hexBuilding = (ContextCompat.getDrawable(GameApplication.instance, R.drawable.hex_building) as Drawable)

    fun initialize(w: Int, h: Int, size: Int, paint: Paint) {
        if (!::hexLayout.isInitialized) {
            height = size.toFloat()
            width = height * (312f / 360f)
            hexLayout = Hex.Layout(Hex.Orientation.layoutPointy,
                    Hex.Point(size.toDouble(), size.toDouble()),
                    Hex.Point(w / 2.0, h / 2.0))
            paint.strokeWidth = Math.max(1f, size * 0.03f)
            paint.textSize = height * 0.3f
        }
    }

    fun getHexByPoint(x: Float, y: Float): Hex {
        return hexLayout.pixelToHex(Hex.Point(x.toDouble(), y.toDouble()))
    }

    fun drawBuilding(building: Building?, hex: Hex, canvas: Canvas, paint: Paint) {
        val points = hexLayout.polygonCorners(hex)
        val dp = hexLayout.hexToPixel(hex)
        rect.set((dp.x - width).toInt(), (dp.y - height).toInt(), (dp.x + width).toInt(), (dp.y + height).toInt())
        hexTile.bounds = rect
        hexTile.draw(canvas)
        if (building != null) {
            val tx = (dp.x - width * 0.9f).toFloat()
            val ty = (dp.y + height * 0.45f).toFloat()
            hexBuilding.bounds = rect
            hexBuilding.draw(canvas)
            val prevColor = paint.color
            paint.color = Colors.colorPrimaryDark
            canvas.drawText(building.name, tx + 3f, ty + 3f, paint)
            paint.color = Colors.lightBlue
            canvas.drawText(building.name, tx, ty, paint)
            paint.color = prevColor
        }

        for (i in 0 until 6) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            canvas.drawLine(p1.x.toFloat(), p1.y.toFloat(),
                    p2.x.toFloat(), p2.y.toFloat(), paint)
        }
    }
}