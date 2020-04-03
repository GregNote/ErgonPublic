package pl.garedgame.game.collisions

class QuadTree(
        private val boundary: Rectangle,
        private val capacity: Int,
        private val level: Int = 0
) {
    private var divided = false
    private var childrenCreated = false
    private lateinit var northWest: QuadTree
    private lateinit var northEast: QuadTree
    private lateinit var southWest: QuadTree
    private lateinit var southEast: QuadTree
    private val objects = ArrayList<Polygon>()

    private fun subdivide() {
        if (!divided) {
            if (!childrenCreated) {
                val w2 = boundary.w / 2f
                val h2 = boundary.h / 2f
                northWest = QuadTree(Rectangle(boundary.x - w2, boundary.y - h2, w2, h2), capacity, level + 1)
                northEast = QuadTree(Rectangle(boundary.x + w2, boundary.y - h2, w2, h2), capacity, level + 1)
                southWest = QuadTree(Rectangle(boundary.x - w2, boundary.y + h2, w2, h2), capacity, level + 1)
                southEast = QuadTree(Rectangle(boundary.x + w2, boundary.y + h2, w2, h2), capacity, level + 1)
                childrenCreated = true
            }
            divided = true
        }
    }

    fun query(range: Range): ArrayList<Polygon> {
        val result = arrayListOf<Polygon>()
        if (!boundary.intersect(range)) return result
        else {
            result.addAll(objects)
            if (divided) {
                result.addAll(northWest.query(range))
                result.addAll(northEast.query(range))
                result.addAll(southWest.query(range))
                result.addAll(southEast.query(range))
            }
        }
        return result
    }

    fun all(): ArrayList<Polygon> {
        val result = arrayListOf<Polygon>()
        result.addAll(objects)
        if (divided) {
            result.addAll(northWest.all())
            result.addAll(northEast.all())
            result.addAll(southWest.all())
            result.addAll(southEast.all())
        }
        return result
    }

    fun add(polygon: Polygon): Boolean {
        val isContain = boundary.contain(polygon)
        if (isContain) {
            if (!divided && objects.size < capacity && objects.add(polygon)) return true
            if (objects.size >= capacity) {
                subdivide()
                val itObjects = objects.iterator()
                for (ob in itObjects) {
                    if (northWest.add(ob) || northEast.add(ob) ||
                            southWest.add(ob) || southEast.add(ob)) {
                        itObjects.remove()
                    }
                }
            }
            if (divided && (northWest.add(polygon) || northEast.add(polygon) ||
                            southWest.add(polygon) || southEast.add(polygon))) {
                return true
            }
        }
        if ((level == 0 || isContain) && objects.add(polygon)) return true
        return false
    }

    fun remove(polygon: Polygon): Boolean {
        return if (divided && (northWest.remove(polygon) || northEast.remove(polygon) ||
                        southWest.remove(polygon) || southEast.remove(polygon) || objects.remove(polygon))) {
            true
        } else objects.remove(polygon)
    }

    fun isEmpty(): Boolean {
        return !divided && objects.isEmpty()
    }

    fun update(buffer: ArrayList<Polygon> = arrayListOf()) {
        val itObjects = objects.iterator()
        for (obj in itObjects) {
            if (!boundary.contain(obj) && buffer.add(obj)) {
                itObjects.remove()
            } else if (divided && (northWest.add(obj) || northEast.add(obj) ||
                            southWest.add(obj) || southEast.add(obj))) {
                itObjects.remove()
            }
        }
        if (divided) {
            northWest.update(buffer)
            northEast.update(buffer)
            southWest.update(buffer)
            southEast.update(buffer)
            val itBuffer = buffer.iterator()
            for (obj in itBuffer) {
                if (northWest.add(obj) || northEast.add(obj) ||
                        southWest.add(obj) || southEast.add(obj)) {
                    itBuffer.remove()
                }
            }
        }
        if (level == 0) {
            for (obj in buffer) objects.add(obj)
        } else {
            if (divided && northWest.isEmpty() && northEast.isEmpty() && southWest.isEmpty() && southEast.isEmpty() && objects.isEmpty()) {
                divided = false
            }
        }
    }
}
