package pl.garedgame.game.pathfinding

import pl.garedgame.game.Vector2

class Node {
    val pos: Vector2 = Vector2()
    var walkable: Boolean = true
    var gridX = 0
    var gridY = 0

    var parent: Node? = null
    var gCost = 0// distance from start node
    var hCost = 0// distance from end node
    val fCost: Int
        get() { return gCost + hCost }

    fun clear() {
        parent = null
        gCost = 0
        hCost = 0
    }

    override fun toString(): String {
        return "Node(gridX:$gridX gridY:$gridY pos:$pos gCost:$gCost hCost:$hCost fCost:$fCost)"
    }
}
