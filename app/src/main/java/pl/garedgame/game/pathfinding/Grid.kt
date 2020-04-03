package pl.garedgame.game.pathfinding

import androidx.core.math.MathUtils
import pl.garedgame.game.Vector2
import pl.garedgame.game.util.Utils
import kotlin.math.round

class Grid(
        private val gridW: Float,
        private val gridH: Float,
        private val nodeRadius: Float,
        val checkCollision: (Vector2) -> Boolean
) : PathFinding.NodeProvider {

    private val nodeDiameter = nodeRadius * 2f
    private val gridSizeX: Int = round(gridW / nodeDiameter).toInt()
    private val gridSizeY: Int = round(gridH / nodeDiameter).toInt()
    private val grid: Array<Node> = Array(gridSizeY * gridSizeX) { Node() }

    init {
        createGrid()
    }

    private fun createGrid() {
        for (x in 0 until gridSizeX) {
            for (y in 0 until gridSizeY) {
                grid[y * gridSizeX + x].also { node ->
                    node.gridX = x
                    node.gridY = y
                    node.pos.x = (x - gridSizeX / 2) * nodeDiameter + nodeRadius
                    node.pos.y = (y - gridSizeY / 2) * nodeDiameter + nodeRadius
                }
            }
        }
    }

    fun getRandomNode(): Node {
        val walkable = grid.filter { it.walkable }
        return walkable[Utils.rand.nextInt(walkable.size)]
    }

    override fun getNeighbours(node: Node): ArrayList<Node> {
        val neighbours = arrayListOf<Node>()
        for (x in -1..1) for (y in -1..1) {
            get(node.gridX + x, node.gridY + y)?.apply {
                neighbours.add(this)
            }
        }
        return neighbours
    }

    fun updateGrid() {
        forEach { node -> node.walkable = !checkCollision(node.pos) }
    }

    override fun clearNodes() {
        forEach { node -> node.clear() }
    }

    override fun getNodeByPosition(position: Vector2): Node? {
        return get(position)
    }

    fun forEach(action: (Node) -> Unit) {
        for (node in grid) action(node)
    }

    operator fun get(pos: Vector2): Node? {
        val percentX = MathUtils.clamp((pos.x + gridW / 2) / gridW, 0f, 1f)
        val percentY = MathUtils.clamp((pos.y + gridH / 2) / gridH, 0f, 1f)
        val x = (gridSizeX * percentX).toInt()
        val y = (gridSizeY * percentY).toInt()
        return get(x, y)
    }

    operator fun get(x: Int, y: Int): Node? {
        return if (x in 0 until gridSizeX && y in 0 until gridSizeY) {
            grid[y * gridSizeX + x]
        } else null
    }
}
