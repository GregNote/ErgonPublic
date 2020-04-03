package pl.garedgame.game

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class Hex(var q: Int, var r: Int, var s: Int) {

    companion object {
        val directions = arrayOf(
                Hex(1, 0, -1), Hex(1, -1, 0), Hex(0, -1, 1),
                Hex(-1, 0, 1), Hex(-1, 1, 0), Hex(0, 1, -1)
        )
        private fun ring(center: Hex, radius: Int): ArrayList<Hex> {
            val result = arrayListOf<Hex>()

            var hex = center
            for (i in 0 until radius) hex = hex.neighbor(4)
            for (i in 0 until 6) {
                for (j in 0 until radius) {
                    result.add(hex)
                    hex = hex.neighbor(i)
                }
            }
            return result
        }
        fun spiral(center: Hex, radius: Int): ArrayList<Hex> {
            val result = arrayListOf(center)
            for (i in 1..radius) result.addAll(ring(center, i))
            return result
        }
        fun round(q: Double, r: Double, s: Double): Hex {
            var rq = q.roundToInt()
            var rr = r.roundToInt()
            var rs = s.roundToInt()

            val qDiff = abs(rq - q)
            val rDiff = abs(rr - r)
            val sDiff = abs(rs - s)

            if (qDiff > rDiff && rDiff > sDiff) {
                rq = -rr - rs
            } else if (rDiff > sDiff) {
                rr = -rq - rs
            } else {
                rs = -rq - rr
            }
            return Hex(rq, rr, rs)
        }
    }
    private fun length(): Float {
        return (abs(q) + abs(r) + abs(s)) / 2f
    }
    fun distance(other: Hex): Float {
        return (this - other).length()
    }
    private fun direction(direction: Int): Hex {
        assert (direction in 0..5)
        return directions[direction]
    }
    fun neighbor(direction: Int): Hex {
        return this + direction(direction)
    }
    override fun equals(other: Any?): Boolean {
        return if (other is Hex) {
            q == other.q && r == other.r && s == other.s
        } else super.equals(other)
    }
    operator fun plus(other: Hex): Hex {
        return Hex(q + other.q, r + other.r, s + other.s)
    }
    operator fun minus(other: Hex): Hex {
        return Hex(q - other.q, r - other.r, s - other.s)
    }
    operator fun times(value: Float): Hex {
        return Hex((q * value).toInt(), (r * value).toInt(), (s * value).toInt())
    }
    override fun hashCode(): Int {
        var result = q.hashCode()
        result = 31 * result + r.hashCode()
        result = 31 * result + s.hashCode()
        return result
    }

    data class Point(var x: Double, var y: Double)

    class Orientation(
            val f0: Double, val f1: Double, val f2: Double, val f3: Double,
            val b0: Double, val b1: Double, val b2: Double, val b3: Double,
            val startAngle: Double
    ) {
        companion object {
            val layoutPointy = Orientation(sqrt(3.0), sqrt(3.0) / 2.0, 0.0, 3.0 / 2.0,
                    sqrt(3.0) / 3.0, -1.0 / 3.0, 0.0, 2.0 / 3.0,
                    0.5)
            val layoutFlat = Orientation(3.0 / 2.0, 0.0, sqrt(3.0) / 2.0, sqrt(3.0),
                    2.0 / 3.0, 0.0, -1.0 / 3.0, sqrt(3.0) / 3.0,
                    0.0)
        }
    }

    class Layout(val orientation: Orientation, val size: Point, val origin: Point) {

        fun hexToPixel(hex: Hex): Point {
            val x = (orientation.f0 * hex.q + orientation.f1 * hex.r) * size.x
            val y = (orientation.f2 * hex.q + orientation.f3 * hex.r) * size.y
            return Point(x + origin.x, y + origin.y)
        }

        fun pixelToHex(point: Point): Hex {
            val pt = Point((point.x - origin.x) / size.x, (point.y - origin.y) / size.y)
            val q = orientation.b0 * pt.x + orientation.b1 * pt.y
            val r = orientation.b2 * pt.x + orientation.b3 * pt.y
            return round(q, r, (-q - r))
        }

        private fun hexCornerOffset(corner: Int): Point {
            val angle = 2.0 * Math.PI * (orientation.startAngle + corner) / 6
            return Point(size.x * Math.cos(angle), size.y * sin(angle))
        }

        fun polygonCorners(hex: Hex): ArrayList<Point> {
            val corners = arrayListOf<Point>()
            val center = hexToPixel(hex)
            for (i in 0 until 6) {
                val offset = hexCornerOffset(i)
                corners.add(Point(center.x + offset.x, center.y + offset.y))
            }
            return corners
        }
    }
}