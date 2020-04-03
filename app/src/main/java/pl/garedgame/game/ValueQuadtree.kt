package pl.garedgame.game

import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.collisions.Range
import pl.garedgame.game.collisions.Rectangle
import pl.garedgame.game.collisions.SquarePolygon

class ValueQuadtree<T : Number>(
        val boundary: Rectangle,
        var clearValue: T,
        private val level: Int = 0,
        val maxLevel: Int = 6
) {
    var value: T = clearValue
    private val polygon = SquarePolygon()

    private var divided = false
    private lateinit var northWest: ValueQuadtree<T>
    private lateinit var northEast: ValueQuadtree<T>
    private lateinit var southWest: ValueQuadtree<T>
    private lateinit var southEast: ValueQuadtree<T>

    init {
        polygon.orientation.pos.x = boundary.x
        polygon.orientation.pos.y = boundary.y
        polygon.orientation.scaleX = boundary.w * 2f
        polygon.orientation.scaleY = boundary.h * 2f
        polygon.update()
        prepareChildren()
    }

    private fun prepareChildren() {
        if (level < maxLevel) {
            val w2 = boundary.w / 2f
            val h2 = boundary.h / 2f
            northWest = ValueQuadtree(Rectangle(boundary.x - w2, boundary.y - h2, w2, h2), clearValue, level + 1)
            northEast = ValueQuadtree(Rectangle(boundary.x + w2, boundary.y - h2, w2, h2), clearValue, level + 1)
            southWest = ValueQuadtree(Rectangle(boundary.x - w2, boundary.y + h2, w2, h2), clearValue, level + 1)
            southEast = ValueQuadtree(Rectangle(boundary.x + w2, boundary.y + h2, w2, h2), clearValue, level + 1)
        }
    }

    private fun subdivide() {
        if (!divided && level < maxLevel) {
            divided = true
            northWest.value = clearValue
            northEast.value = clearValue
            southWest.value = clearValue
            southEast.value = clearValue
        }
    }

    fun insert(range: Range, newVal: T) {
        if (boundary.intersect(range)) {
            subdivide()
            if (divided) {
                northWest.insert(range, newVal)
                northEast.insert(range, newVal)
                southWest.insert(range, newVal)
                southEast.insert(range, newVal)
            } else if (range.contain(boundary.x, boundary.y)) {
                value = newVal
            }
        }
    }

    private fun intersect(polygon: Polygon): Boolean {
        return polygon.overlap(this.polygon)
    }

    fun insert(polygon: Polygon, newVal: T) {
        if (intersect(polygon)) {
            subdivide()
            if (divided) {
                northWest.insert(polygon, newVal)
                northEast.insert(polygon, newVal)
                southWest.insert(polygon, newVal)
                southEast.insert(polygon, newVal)
            } else {
                value = newVal
            }
        }
    }

    fun onEveryLeaf(callback: (ValueQuadtree<*>) -> Unit) {
        if (divided) {
            northWest.onEveryLeaf(callback)
            northEast.onEveryLeaf(callback)
            southWest.onEveryLeaf(callback)
            southEast.onEveryLeaf(callback)
        } else {
            callback(this)
        }
    }

    fun onEvery(callback: (ValueQuadtree<*>) -> Unit) {
        callback(this)
        if (level < maxLevel) {
            northWest.onEvery(callback)
            northEast.onEvery(callback)
            southWest.onEvery(callback)
            southEast.onEvery(callback)
        }
    }

    fun perValue(callback: (T) -> T) {
        if (divided) {
            northWest.perValue(callback)
            northEast.perValue(callback)
            southWest.perValue(callback)
            southEast.perValue(callback)
        } else {
            value = callback(value)
        }
    }

    fun simplify() {
        if (divided) {
            northWest.simplify()
            northEast.simplify()
            southWest.simplify()
            southEast.simplify()
            if (
                    !northWest.divided && !northEast.divided &&
                    !southWest.divided && !southEast.divided &&
                    northWest.value == northEast.value &&
                    northEast.value == southWest.value &&
                    southWest.value == southEast.value
            ) {
                value = northWest.value
                divided = false
            }
        }
    }

    fun checkUpdate(isVisible: (poly: Polygon) -> T) {
        subdivide()
        if (divided) {
            northWest.checkUpdate(isVisible)
            northEast.checkUpdate(isVisible)
            southWest.checkUpdate(isVisible)
            southEast.checkUpdate(isVisible)
        } else value = isVisible(polygon)
    }

    fun clear(clear: T) {
        value = clear
        if (level < maxLevel) {
            northWest.clear(clear)
            northEast.clear(clear)
            southWest.clear(clear)
            southEast.clear(clear)
        }
    }
}
