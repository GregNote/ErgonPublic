package pl.garedgame.game

import com.google.gson.annotations.Expose
import pl.garedgame.game.util.squared
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class Vector3(
        @Expose var x: Float = 0f,
        @Expose var y: Float = 0f,
        @Expose var z: Float = 0f
) {

    constructor(vec: Vector3) : this(vec.x, vec.y, vec.z)

    fun set(_x: Float, _y: Float, _z: Float) {
        x = _x
        y = _y
        z = _z
    }

    fun set(v: Vector3) {
        x = v.x
        y = v.y
        z = v.z
    }

    fun normalize() {
        val length = length()
        if (length != 0f) {
            x /= length
            y /= length
            z /= length
        }
    }

    fun length(): Float = sqrt((x.squared() + y.squared() + z.squared()).toDouble()).toFloat()

    fun round() {
        x = round(x)
        y = round(y)
        z = round(z)
    }

    fun rounded(): Vector3 {
        return Vector3(x, y, z).apply { round() }
    }

    fun rotateX(angle: Float) {
        val rad = Math.toRadians(angle.toDouble())
        val py = y * cos(rad) - z * sin(rad)
        val pz = y * sin(rad) + z * cos(rad)
        y = py.toFloat()
        z = pz.toFloat()
    }

    fun rotateY(angle: Float) {
        val rad = Math.toRadians(angle.toDouble())
        val px = x * cos(rad) + z * sin(rad)
        val pz = (-x) * sin(rad) + z * cos(rad)
        x = px.toFloat()
        z = pz.toFloat()
    }

    fun rotateZ(angle: Float) {
        val rad = Math.toRadians(angle.toDouble())
        val px = x * cos(rad) - y * sin(rad)
        val py = x * sin(rad) + y * cos(rad)
        x = px.toFloat()
        y = py.toFloat()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Vector3) {
            x == other.x && y == other.y && z == other.z
        } else super.equals(other)
    }

    operator fun timesAssign(value: Float) {
        x *= value
        y *= value
        z *= value
    }

    operator fun times(value: Float): Vector3 {
        return Vector3(x * value, y * value, z * value)
    }

    operator fun divAssign(value: Float) {
        x /= value
        y /= value
        z /= value
    }

    operator fun div(value: Float): Vector3 {
        return Vector3(x / value, y / value, z / value)
    }

    operator fun plusAssign(vec: Vector3) {
        x += vec.x
        y += vec.y
        z += vec.z
    }

    operator fun plus(position: Vector3): Vector3 {
        return Vector3(x + position.x, y + position.y, z + position.z)
    }

    operator fun minus(position: Vector3): Vector3 {
        return Vector3(x - position.x, y - position.y, z - position.z)
    }

    operator fun unaryMinus(): Vector3 {
        return Vector3(-x, -y, -z)
    }
}
