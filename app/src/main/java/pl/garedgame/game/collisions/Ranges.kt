package pl.garedgame.game.collisions

import pl.garedgame.game.Vector2
import pl.garedgame.game.util.squared
import kotlin.math.abs

abstract class Range(
        var x: Float,
        var y: Float
) {
    companion object {
        fun rectangleCircleIntersect(rectangle: Rectangle, circle: Circle): Boolean {
            val xDist = abs(rectangle.x - circle.x)
            val yDist = abs(rectangle.y - circle.y)
            if (xDist > rectangle.w + circle.r) return false
            if (yDist > rectangle.h + circle.r) return false
            if (xDist <= rectangle.w) return true
            if (yDist <= rectangle.h) return true
            val xCorDist = xDist - rectangle.w
            val yCorDist = yDist - rectangle.h
            val squaredCorDist = xCorDist.squared() + yCorDist.squared()
            return squaredCorDist <= circle.r.squared()
        }
    }

    abstract fun contain(x: Float, y: Float): Boolean
    abstract fun contain(pos: Vector2): Boolean
    abstract fun intersect(other: Range): Boolean
    fun contain(polygon: Polygon): Boolean {
        polygon.update()
        for (v in polygon.p) if (!contain(v)) return false
        return true
    }
}

open class Rectangle(
        x: Float,
        y: Float,
        var w: Float,
        var h: Float
) : Range(x, y) {
    val xRange = (x - w)..(x + w)
    val yRange = (y - h)..(y + h)

    override fun contain(x: Float, y: Float): Boolean {
        return x in xRange && y in yRange
    }

    override fun contain(pos: Vector2): Boolean {
        return pos.x in xRange && pos.y in yRange
    }

    override fun intersect(other: Range): Boolean {
        return when (other) {
            is Rectangle -> {
                !( (other.x - other.w) > (x + w) || (x - w) > (other.x + other.w) ||
                        (other.y - other.h) > (y + h) || (y - h) > (other.y + other.h) )
            }
            is Circle -> rectangleCircleIntersect(this, other)
            else -> false
        }
    }
}

class Circle(
        x: Float,
        y: Float,
        var r: Float
) : Range(x, y) {

    override fun contain(x: Float, y: Float): Boolean {
        return (this.x - x).squared() + (this.y - y).squared() <= r.squared()
    }

    override fun contain(pos: Vector2): Boolean {
        return (x - pos.x).squared() + (y - pos.y).squared() <= r.squared()
    }

    override fun intersect(other: Range): Boolean {
        return when (other) {
            is Circle -> {
                (this.x - other.x).squared() + (this.y - other.y).squared() <= (this.r + other.r).squared()
            }
            is Rectangle -> rectangleCircleIntersect(other, this)
            else -> false
        }
    }
}
