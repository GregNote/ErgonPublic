package pl.garedgame.game.pathfinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.render.GameViewGL
import kotlin.math.abs

class PathFinding(private val nodeProvider: NodeProvider) {

    @Volatile private var isProcessing = false
    private val requests = mutableListOf<Request>()

    class Request(
            val gameUnit: GameUnit,
            val endPos: Vector2,
            val callback: (ArrayList<Vector2>) -> Unit
    ) {
        var aborted = false
    }

    private fun tryProcessNext() {
        if (!isProcessing) requests.firstOrNull()?.also { first ->
            requests.remove(first)
            if (first.aborted) {
                GameViewGL.post { tryProcessNext() }
            } else {
                isProcessing = true
                GlobalScope.launch(Dispatchers.Default) {
                    val path = processPathFinding(first)
                    finishProcessingPath(first, path)
                }
            }
        }
    }

    private fun finishProcessingPath(request: Request, path: ArrayList<Vector2>) {
        isProcessing = false
        GameViewGL.post {
            request.callback.invoke(path)
            tryProcessNext()
        }
    }

    fun findPath(gameUnit: GameUnit, endPos: Vector2, callback: (ArrayList<Vector2>) -> Unit): Request {
        val request = Request(gameUnit, endPos, callback)
        requests.add(request)
        GameViewGL.post { tryProcessNext() }
        return request
    }

    private fun processPathFinding(request: Request): ArrayList<Vector2> {
        val startNode = nodeProvider.getNodeByPosition(request.gameUnit.orientation.pos)
        val endNode = nodeProvider.getNodeByPosition(request.endPos)
        if (startNode != null && startNode.walkable && endNode != null && endNode.walkable) {
            val openSet = mutableSetOf<Node>()
            val closeSet = mutableSetOf<Node>()
            openSet.add(startNode)

            while (openSet.isNotEmpty()) {
                val node = openSet.minBy { it.fCost }!!
                openSet.remove(node)
                closeSet.add(node)

                if (node == endNode) {
                    val result = retracePath(startNode, endNode)
                    if (result.isNotEmpty()) result[result.size - 1] = request.endPos
                    return result
                }

                nodeProvider.getNeighbours(node)
                        .filterNot { closeSet.contains(it) || !it.walkable }
                        .forEach { neighbour ->
                            val newCostToNeighbour = node.gCost + getDistance(node, neighbour)
                            if (newCostToNeighbour < neighbour.gCost || !openSet.contains(neighbour)) {
                                neighbour.gCost = newCostToNeighbour
                                neighbour.hCost = getDistance(neighbour, endNode)
                                neighbour.parent = node

                                if (!openSet.contains(neighbour)) {
                                    openSet.add(neighbour)
                                }
                            }
                        }
                if (request.aborted) {
                    return arrayListOf()
                }
            }
        }
        return arrayListOf()
    }

    private fun retracePath(startNode: Node, endNode: Node): ArrayList<Vector2> {
        val path = arrayListOf<Vector2>()
        var currentNode: Node = endNode
        while (currentNode != startNode) {
            path.add(currentNode.pos)
            currentNode = currentNode.parent!!
        }
        path.reverse()
        return path
    }

    private fun getDistance(nodeA: Node, nodeB: Node): Int {
        val distX = abs(nodeA.gridX - nodeB.gridX)
        val distY = abs(nodeA.gridY - nodeB.gridY)
        return if (distX > distY) {
            14 * distY + 10 * (distX - distY)
        } else {
            14 * distX + 10 * (distY - distX)
        }
    }

    interface NodeProvider {
        fun getNodeByPosition(position: Vector2): Node?
        fun clearNodes()
        fun getNeighbours(node: Node): ArrayList<Node>
    }
}
