package pl.garedgame.game.entities

import pl.garedgame.game.Vector2
import pl.garedgame.game.collisions.Circle
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.collisions.QuadTree
import pl.garedgame.game.collisions.Rectangle
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.pathfinding.Grid
import pl.garedgame.game.pathfinding.Node
import pl.garedgame.game.pathfinding.PathFinding
import pl.garedgame.game.render.*
import pl.garedgame.game.skirmish.SceneMap
import pl.garedgame.game.skirmish.Skirmish
import kotlin.math.max
import kotlin.math.min

open class GameScene(val width: Float, val height: Float, val nodeRadius: Float) : Entity() {

    private val quadTree: QuadTree

    private val units = arrayListOf<GameUnit>()
    private val items = arrayListOf<ItemObject>()
    private val terrains = arrayListOf<Terrain>()
    private val obstacles = arrayListOf<Obstacle>()

    private val visibleUnitsBatch = TextureSpriteBatch()
    private val sensedUnitsBatch = TextureSpriteBatch()
    private val playerUnitsBatch = TextureSpriteBatch()
    private val itemsBatch = TextureSpriteBatch()
    private val terrainBatch = TextureSpriteBatch()
    private val obstaclesBatch = TextureSpriteBatch()
    private val effectsBatch = ColorSpriteBatch()

    private val unitsTexture = OpenGLRenderer.getTexture("units.png")
    private val terrainTexture = OpenGLRenderer.getTexture("terrain.png")
    private val obstaclesTexture = OpenGLRenderer.getTexture("objects.png")

    private val viewMesh = ViewMesh()
    private val fovSprite = ColorSprite(0f, 0f, 0f, 0.3f).apply {
        orientation.scaleX = width
        orientation.scaleY = height
    }

    var isEditor = false
    var drawShadows = true

    constructor(sceneMap: SceneMap) : this(sceneMap.width, sceneMap.height, sceneMap.nodeRadius) {
        addAll(sceneMap.objects)
    }

    init {
        val qtSize = (max(width, height) / 2f)
        quadTree = QuadTree(Rectangle(0f, 0f, qtSize, qtSize), 2)
    }

    private val grid: Grid by lazy {
        Grid(width, height, nodeRadius) { position ->
            if (!isEditor) quadTree.query(Circle(position.x, position.y, 1f)).forEach {
                val gObj = it.gameObject
                if (gObj is WithPolygon && !gObj.dynamic && it.contain(position)) {
                    return@Grid true
                }
            }
            return@Grid false
        }
    }

    private val pathFinding: PathFinding by lazy {
        PathFinding(grid)
    }

    fun addAll(entities: Array<GameObject>) {
        for (entity in entities) {
            super.add(entity).also {
                when (entity) {
                    is GameUnit -> units.add(entity)
                    is Obstacle -> obstacles.add(entity)
                    is Terrain -> terrains.add(entity)
                    is ItemObject -> items.add(entity)
                }
                if (entity is WithPolygon) quadTree.add(entity.polygon)
            }
        }
        quadTree.update()
        if (!isEditor) grid.updateGrid()
    }

    override fun add(entity: Entity): Boolean {
        return super.add(entity).also {
            when (entity) {
                is GameUnit -> units.add(entity)
                is Obstacle -> obstacles.add(entity)
                is Terrain -> terrains.add(entity)
                is ItemObject -> items.add(entity)
            }
            if (entity is WithPolygon) {
                quadTree.add(entity.polygon)
                quadTree.update()
                if (!isEditor) grid.updateGrid()
            }
        }
    }

    override fun remove(entity: Entity): Boolean {
        return super.remove(entity).also {
            when (entity) {
                is GameUnit -> units.remove(entity)
                is Obstacle -> obstacles.remove(entity)
                is Terrain -> terrains.remove(entity)
                is ItemObject -> items.remove(entity)
            }
            if (entity is WithPolygon) {
                quadTree.remove(entity.polygon)
                quadTree.update()
                if (!isEditor) grid.updateGrid()
            }
        }
    }

    fun getObjectsArrayList(): ArrayList<GameObject> {
        val result = arrayListOf<GameObject>()
        result.addAll(terrains)
        result.addAll(obstacles)
        return result
    }

    fun getObjectsArray(): Array<GameObject> {
        val arrayList = getObjectsArrayList()
        return Array(arrayList.size) {
            arrayList[it]
        }
    }

    fun findTaggedObjects(tag: String): ArrayList<GameObject> {
        val objects = getObjectsArrayList()
        val tagged = objects.filter { it.tag == tag }
        return arrayListOf<GameObject>().apply {
            addAll(tagged)
        }
    }

    fun moveTerrainUp(terrain: Terrain): Boolean {
        val index = terrains.indexOf(terrain)
        return if (index in terrains.indices && index >= 1) {
            val tmp = terrains[index - 1]
            terrains[index - 1] = terrains[index]
            terrains[index] = tmp
            true
        } else false
    }

    fun moveTerrainDown(terrain: Terrain): Boolean {
        val index = terrains.indexOf(terrain)
        return if (index in terrains.indices && index < terrains.size - 1) {
            val tmp = terrains[index + 1]
            terrains[index + 1] = terrains[index]
            terrains[index] = tmp
            true
        } else false
    }

    fun update(sinceMillis: Long) {
        val visibilityOfAll = if(Skirmish.instance.playerController.isAnyAlive()) GameUnit.Visibility.Invisible else GameUnit.Visibility.Visible
        units.forEach {
            it.update(sinceMillis)
            it.visible = visibilityOfAll
            if (it.dynamic) checkCollisions(it)
        }
        Skirmish.instance.playerController.getAllUnits().forEach { playerUnit ->
            if (playerUnit.isOperative()) {
                playerUnit.visible = GameUnit.Visibility.Visible
                playerUnit.unitsInSight().forEach { it.visible = GameUnit.Visibility.Visible }
            }
        }
        quadTree.update()
    }

    private fun checkCollisions(gameObject: GameObject) {
        quadTree.query(Rectangle(
                gameObject.orientation.pos.x, gameObject.orientation.pos.y,
                gameObject.orientation.scaleX, gameObject.orientation.scaleX
        )).forEach { otherPolygon ->
            if (gameObject is WithPolygon && gameObject.collide && gameObject.polygon != otherPolygon && gameObject.polygon.overlap(otherPolygon)) {
                val otherObject = otherPolygon.gameObject
                if (otherObject is WithPolygon && otherObject.collide) {
                    if (otherObject.dynamic) {
                        otherPolygon.orientation.pos.x += gameObject.polygon.overlapDisplace.x * 0.5f
                        otherPolygon.orientation.pos.y += gameObject.polygon.overlapDisplace.y * 0.5f
                        gameObject.orientation.pos.x -= gameObject.polygon.overlapDisplace.x * 0.5f
                        gameObject.orientation.pos.y -= gameObject.polygon.overlapDisplace.y * 0.5f
                    } else {
                        gameObject.orientation.pos.x -= gameObject.polygon.overlapDisplace.x
                        gameObject.orientation.pos.y -= gameObject.polygon.overlapDisplace.y
                    }
                    gameObject.onCollision(otherObject)
                    otherObject.onCollision(gameObject)
                }
            }
        }
    }

    fun rayCast(
            rayStart: Vector2,
            rayEnd: Vector2,
            predicate: (Polygon) -> Boolean
    ): Pair<Polygon, Vector2>? {
        val intersections = arrayListOf<Pair<Polygon, Vector2>>()
        quadTree.query(Rectangle(
                (rayStart.x + rayEnd.x) / 2f,
                (rayStart.y + rayEnd.y) / 2f,
                max(rayStart.x, rayEnd.x) - min(rayStart.x, rayEnd.x),
                max(rayStart.y, rayEnd.y) - min(rayStart.y, rayEnd.y)
        )).filter(predicate).forEach { polygon ->
            polygon.rayCast(rayStart, rayEnd)?.also {
                intersections.add(polygon to it)
            }
        }
        return intersections.minBy { rayStart.vectorTo(it.second).length() }
    }

    fun findPath(gameUnit: GameUnit, to: Vector2, callback: (ArrayList<Vector2>) -> Unit): PathFinding.Request {
        return pathFinding.findPath(gameUnit, to, callback)
    }

    fun getNode(position: Vector2) = grid[position]

    fun getNeighbours(node: Node) = grid.getNeighbours(node)

    fun getRandomNode() = grid.getRandomNode()

    fun findUnit(predicate: (GameUnit) -> Boolean): GameUnit? = units.find(predicate)

    fun objectsInRange(x: Float, y: Float, w: Float, h: Float): List<GameObject> {
        return quadTree.query(Rectangle(x, y, w, h)).filter { polygon ->
            polygon.gameObject?.let { true } ?: false
        }.map { it.gameObject as GameObject }
    }

    fun objectsInRange(pos: Vector2, radius: Float): List<GameObject> {
        return quadTree.query(Circle(pos.x, pos.y, radius)).filter { polygon ->
            polygon.gameObject?.let { true } ?: false
        }.map { it.gameObject as GameObject }
    }

    fun obstacleInRange(pos: Vector2, radius: Float): List<Obstacle> {
        return objectsInRange(pos, radius).filterIsInstance<Obstacle>()
    }

    fun unitsInRange(pos: Vector2, radius: Float): List<GameUnit> {
        return objectsInRange(pos, radius).filterIsInstance<GameUnit>()
    }

    fun unitAtPosition(position: Vector2): GameUnit? {
        return unitsInRange(position, 0.01f).minBy {
            Vector2.distance(it.orientation.pos, position)
        }
    }

    open fun draw(renderer: OpenGLRenderer) {
        terrainBatch.clearSprites()
        itemsBatch.clearSprites()
        obstaclesBatch.clearSprites()
        effectsBatch.clearSprites()
        viewMesh.clearUnits()
        visibleUnitsBatch.clearSprites()
        sensedUnitsBatch.clearSprites()
        playerUnitsBatch.clearSprites()

        items.forEach { item -> itemsBatch.add(item.mesh) }
        terrains.forEach { terrain -> terrainBatch.add(terrain.mesh) }
        obstacles.forEach { obstacle -> obstaclesBatch.add(obstacle.mesh) }
        units.forEach { unit ->
            when (unit.visible) {
                GameUnit.Visibility.Visible -> unit.meshes.forEach { visibleUnitsBatch.add(it) }
                GameUnit.Visibility.Sensed -> unit.meshes.forEach { sensedUnitsBatch.add(it) }
                GameUnit.Visibility.Invisible -> {}
            }
            unit.effects.forEach { effect -> effectsBatch.add(effect.draw()) }
        }
        Skirmish.instance.playerController.getAllUnits().forEach { unit ->
            if (unit.isOperative()) {
                viewMesh.add(unit)
                playerUnitsBatch.add(unit.mesh)
            }
        }
        renderer.mvpMatrixWithCamera().also { mvpMatrix ->
            terrainTexture.bind()
            terrainBatch.draw(mvpMatrix)
            obstaclesTexture.bind()
            obstaclesBatch.draw(mvpMatrix)
            if (drawShadows) {
                unitsTexture.bind()
                sensedUnitsBatch.draw(mvpMatrix)
                renderer.drawWithMask(
                        drawMask = {
                            Texture.Transparent.bind()
                            viewMesh.draw(mvpMatrix)
                            playerUnitsBatch.draw(mvpMatrix)
                        },
                        drawEqualToMask = {
                            Texture.Frame.bind()
                            itemsBatch.draw(mvpMatrix)
                            unitsTexture.bind()
                            visibleUnitsBatch.draw(mvpMatrix)
                            effectsBatch.draw(mvpMatrix)
                        },
                        drawNotEqualToMask = {
                            fovSprite.draw(mvpMatrix)
                        })
            } else {
                unitsTexture.bind()
                visibleUnitsBatch.draw(mvpMatrix)
                sensedUnitsBatch.draw(mvpMatrix)
                effectsBatch.draw(mvpMatrix)
            }
        }
    }
}
