package pl.garedgame.game.collisions

import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.Vector2
import pl.garedgame.game.entities.GameObject
import kotlin.math.max
import kotlin.math.min

class EmptyPolygon : Polygon(
        arrayOf(
                Vector2(-0.5f, -0.5f), Vector2(0.5f, -0.5f),
                Vector2(0f, 0.5f)
        ))

class TrianglePolygon : Polygon(
        arrayOf(
                Vector2(-0.5f, -0.5f), Vector2(0.5f, -0.5f),
                Vector2(0f, 0.5f)
        ))

class DiamondPolygon : Polygon(
        arrayOf(
                Vector2(0f, -0.5f), Vector2(0.5f, 0f),
                Vector2(0f, 0.5f), Vector2(-0.5f, 0f)
        ))

class SquarePolygon : Polygon(
        arrayOf(
                Vector2(-0.5f, -0.5f), Vector2(0.5f, -0.5f),
                Vector2(0.5f, 0.5f), Vector2(-0.5f, 0.5f)
        ))

class PentagonPolygon : Polygon(
        arrayOf(
                Vector2(0f, -0.5f), Vector2(0.5f, 0f),
                Vector2(0.25f, 0.5f), Vector2(-0.25f, 0.5f),
                Vector2(-0.5f, 0f)
        ))

abstract class Polygon(
        val o: Array<Vector2>
) {
    var orientation: SpaceOrientation2D = SpaceOrientation2D()
    val overlapDisplace = Vector2()
    var gameObject: GameObject? = null

    val p: Array<Vector2> = Array(o.size) { Vector2(o[it]) }

    init {
        update()
    }

    fun update() {
        for (i in o.indices) {
            p[i].x = o[i].x * orientation.scaleX
            p[i].y = o[i].y * orientation.scaleY
            p[i].rotate(orientation.rotation)
            p[i].x += orientation.pos.x
            p[i].y += orientation.pos.y
        }
    }

    fun contain(vec: Vector2): Boolean {
        update()
        for (i in p.indices) {
            if (Vector2.findIntersection(orientation.pos, vec, p[i], p[(i + 1) % p.size]) != null) {
                return false
            }
        }
        return true
    }

    fun overlap(polygon: Polygon): Boolean {
        polygon.update()
        update()
        var overlap = Float.POSITIVE_INFINITY
        var poly1 = polygon
        var poly2 = this

        for (shape in 0..1) {
            if (shape == 1) {
                poly1 = this
                poly2 = polygon
            }
            for (a in poly1.p.indices) {
                val b = (a + 1) % poly1.p.size
                val axisProj = Vector2(
                        -(poly1.p[b].y - poly1.p[a].y),
                        (poly1.p[b].x - poly1.p[a].x)
                )

                var minR1 = Float.POSITIVE_INFINITY
                var maxR1 = Float.NEGATIVE_INFINITY
                for (p in poly1.p.indices) {
                    val q = (poly1.p[p].x * axisProj.x + poly1.p[p].y * axisProj.y)
                    minR1 = min(minR1, q)
                    maxR1 = max(maxR1, q)
                }

                var minR2 = Float.POSITIVE_INFINITY
                var maxR2 = Float.NEGATIVE_INFINITY
                for (p in poly2.p.indices) {
                    val q = (poly2.p[p].x * axisProj.x + poly2.p[p].y * axisProj.y)
                    minR2 = min(minR2, q)
                    maxR2 = max(maxR2, q)
                }

                overlap = min(min(maxR1, maxR2) - max(minR1, minR2), overlap)

                if ( !( maxR2 >= minR1 && maxR1 >= minR2 )) return false
            }
        }
        overlapDisplace.set(
                polygon.orientation.pos.x - this.orientation.pos.x,
                polygon.orientation.pos.y - this.orientation.pos.y
        )
        overlapDisplace.normalize()
        overlapDisplace *= overlap
        return true
    }

    fun rayCast(rayStart: Vector2, rayEnd: Vector2): Vector2? {
        val intersections = arrayListOf<Vector2>()
        for (i in p.indices) {
            Vector2.findIntersection(rayStart, rayEnd, p[i], p[(i + 1) % p.size])?.also {
                intersections.add(it)
            }
        }
        return intersections.minBy { rayStart.vectorTo(it).length() }
    }
}
