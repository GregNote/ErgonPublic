package pl.garedgame.game

import com.google.gson.annotations.Expose
import pl.garedgame.game.util.inRange
import pl.garedgame.game.util.squared
import kotlin.math.*

class Vector2(
        @Expose var x: Float = 0f,
        @Expose var y: Float = 0f
) {
    companion object {

        val ZERO: Vector2; get() { return Vector2() }
        val X: Vector2; get() { return Vector2(x = 1f) }
        val Y: Vector2; get() { return Vector2(y = 1f) }

        fun getRandom(pos: Vector2, r: Float): Vector2 {
            return getRandom(pos.x, pos.y, r)
        }

        fun getRandom(x: Float, y: Float, r: Float): Vector2 {
            val a = Math.random() * 2.0 * Math.PI
            val R = r * sqrt(Math.random())
            return Vector2((x + R * cos(a)).toFloat(), (y + R * sin(a)).toFloat())
        }

        fun distance(pos1: Vector2, pos2: Vector2): Float {
            return sqrt(((pos1.x - pos2.x).squared() + (pos1.y - pos2.y).squared()).toDouble()).toFloat()
        }

        fun globeDistance(pos1: Vector2, pos2: Vector2): Float {
            val lat1 = pos1.x.toDouble()
            val lon1 = pos1.y.toDouble()
            val lat2 = pos2.x.toDouble()
            val lon2 = pos2.y.toDouble()

            val phi1 = Math.toRadians(lat1)
            val phi2 = Math.toRadians(lat2)
            val deltaPhi = Math.toRadians(lat2 - lat1)
            val deltaLambda = Math.toRadians(lon2 - lon1)

            val a = sin(deltaPhi/2.0).squared() + cos(phi1) * cos(phi2) * sin(deltaLambda).squared()
            val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

            return (6371e3 * c).toFloat()
        }

        fun findIntersection(s1: Vector2, e1: Vector2, s2: Vector2, e2: Vector2): Vector2? {
            val a1 = e1.y.toDouble() - s1.y.toDouble()
            val b1 = s1.x.toDouble() - e1.x.toDouble()
            val c1 = a1 * s1.x.toDouble() + b1 * s1.y.toDouble()

            val a2 = e2.y.toDouble() - s2.y.toDouble()
            val b2 = s2.x.toDouble() - e2.x.toDouble()
            val c2 = a2 * s2.x.toDouble() + b2 * s2.y.toDouble()

            val delta = a1 * b2 - a2 * b1
            return if (delta == 0.0) null
            else {
                val x = ((b2 * c1 - b1 * c2) / delta).toFloat()
                val y = ((a1 * c2 - a2 * c1) / delta).toFloat()
                if (
                        x.inRange(s1.x, e1.x) && y.inRange(s1.y, e1.y) &&
                        x.inRange(s2.x, e2.x) && y.inRange(s2.y, e2.y)
                ) Vector2(x, y) else null
            }
        }
    }

    constructor(vec: Vector2) : this(vec.x, vec.y)

    fun set(_x: Float, _y: Float) {
        x = _x
        y = _y
    }

    fun set(v: Vector2) {
        x = v.x
        y = v.y
    }

    fun normalize() {
        val length = length()
        if (length != 0f) {
            x /= length
            y /= length
        }
    }

    fun normalized(): Vector2 {
        return Vector2(x, y).apply { normalize() }
    }

    fun length(): Float = sqrt((x.squared() + y.squared()).toDouble()).toFloat()

    fun vectorTo(vectorTo: Vector2, destinationVector: Vector2 = ZERO): Vector2 {
        destinationVector.set(vectorTo.x - this.x, vectorTo.y - this.y)
        return destinationVector
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Vector2) {
            x == other.x && y == other.y
        } else super.equals(other)
    }

    operator fun timesAssign(value: Float) {
        x *= value
        y *= value
    }

    operator fun times(value: Float): Vector2 {
        return Vector2(x * value, y * value)
    }

    operator fun divAssign(value: Float) {
        x /= value
        y /= value
    }

    operator fun div(value: Float): Vector2 {
        return Vector2(x / value, y / value)
    }

    operator fun plusAssign(vec: Vector2) {
        x += vec.x
        y += vec.y
    }

    operator fun plus(position: Vector2): Vector2 {
        return Vector2(x + position.x, y + position.y)
    }

    operator fun minus(position: Vector2): Vector2 {
        return Vector2(x - position.x, y - position.y)
    }

    operator fun unaryMinus(): Vector2 {
        return Vector2(-x, -y)
    }

    fun angleBetween(pos: Vector2): Float {
        val rad = acos(x * pos.x + y * pos.y).toDouble()
        return Math.toDegrees(rad).toFloat()
    }

    fun rotation(): Float {
        return (360f +
                if (x > 0f) angleBetween(-Y)
                else -angleBetween(-Y)
                ) % 360f
    }

    fun rotate(angle: Float) {
        val rad = Math.toRadians(angle.toDouble())
        val newX = cos(rad).toFloat() * x - sin(rad).toFloat() * y
        val newY = sin(rad).toFloat() * x + cos(rad).toFloat() * y
        x = newX
        y = newY
    }

    fun rotated(angle: Float): Vector2 {
        return Vector2(x, y).apply { rotate(angle) }
    }

    fun round() {
        x = round(x)
        y = round(y)
    }

    fun rounded(): Vector2 {
        return Vector2(x, y).apply { round() }
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String {
        return "Pos($x;$y)"
    }
}
