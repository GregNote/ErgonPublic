package pl.garedgame.game.objects

import com.google.gson.annotations.Expose
import pl.garedgame.game.Configuration
import pl.garedgame.game.Game
import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.Vector2
import pl.garedgame.game.behavior.BTNode
import pl.garedgame.game.behavior.Behavior
import pl.garedgame.game.behavior.Sensor
import pl.garedgame.game.collisions.EmptyPolygon
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.entities.Effect
import pl.garedgame.game.entities.Entity
import pl.garedgame.game.entities.GameObject
import pl.garedgame.game.entities.WithPolygon
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.render.TextureSprite
import pl.garedgame.game.skirmish.Skirmish
import java.lang.ref.WeakReference
import kotlin.math.abs

open class GameUnit(
        orientation: SpaceOrientation2D = SpaceOrientation2D(),
        override val polygon: Polygon = EmptyPolygon()
) : GameObject(orientation), WithPolygon {

    companion object {
        var gId = 1L
    }

    @Expose var id = gId++

    @Expose var organisationId = 0L

    @Expose(serialize = false, deserialize = false)
    var assignation: WeakReference<Assignation>? = null

    private var state: UnitState? = null
    protected var behavior: Behavior<*>? = null
    private var behaviorTickGap = Configuration.getBehaviorTickGap()
    private var behaviorTickAcc = 0L
    var behaviorRoot: BTNode? = null
        protected set

    val effects = arrayListOf<Effect>()

    private val unitsInSight = arrayListOf<GameUnit>()
    protected val lookVec = Vector2()

    open var visible = Visibility.Visible
    open val meshes: ArrayList<TextureSprite> = arrayListOf(mesh)

    private val currentPath: ArrayList<Vector2>
        get() {
            getState().let {
                if (it is MoveState) return it.path
            }
            return arrayListOf()
        }

    var selected = false

    override val dynamic: Boolean
        get() = true

    override val collide: Boolean
        get() = true

    fun isSelected(): Boolean {
        return selected || assignation?.get()?.selected ?: false
    }

    open fun listOfSensors() = listOf<Sensor>()

    open fun isOperative() = true

    open fun getSpeed() = 0f

    open fun getRangeOfView() = 0f

    open fun getFieldOfView() = 90f

    open fun takeHealing(healing: Int) { }

    open fun takeDamage(damage: Int) { }

    open fun playerOrder(position: Vector2, ability: Ability?): Boolean = true

    open fun changeState(unitState: UnitState) {
        if (state?.stopable != false) {
            state?.abort()
            state = unitState
        }
    }

    fun getState(): UnitState {
        return state ?: IdleState(this)
    }

    fun stop() {
        changeState(IdleState(this))
    }

    var rotationTo = 0f

    fun lookAt(position: Vector2) {
        rotationTo = lookToRotation(position)
    }

    fun lookAt(gameUnit: GameUnit) {
        rotationTo = lookToRotation(gameUnit.orientation.pos)
    }

    private fun lookToRotation(position: Vector2): Float {
        val vecTo = orientation.pos.vectorTo(position)
        vecTo.normalize()
        return vecTo.rotation()
    }

    fun moveTo(position: Vector2, onDestination: OnStateEndCallback? = null, fromUser: Boolean = false): Boolean {
        getState().also {
            gameScene?.also { gameScene ->
                gameScene.getNode(position)?.also { positionNode ->
                    if (it !is MoveState || gameScene.getNode(it.to) != positionNode) {
                        changeState(
                                MoveState(this, position, onDestination).apply {
                                    itsOrder = fromUser
                                }
                        )
                        return true
                    }
                }
            }
        }
        return false
    }

    protected open fun behaviorTick() {
        unitsInSight.clear()
        behavior?.tick()
    }

    fun changeBehaviorRoot(root: BTNode) {
        behaviorRoot = root
        behavior?.root = root
    }

    fun unitsInSight(): ArrayList<GameUnit> {
        if (unitsInSight.isEmpty()) {
            gameScene?.apply {
                unitsInSight.addAll(
                        unitsInRange(orientation.pos, getRangeOfView()).filter { unit ->
                            if (unit == this@GameUnit) return@filter false
                            val vecTo = orientation.pos.vectorTo(unit.orientation.pos)
                            if (getRangeOfView() < vecTo.length()) return@filter false
                            vecTo.normalize()
                            if (vecTo.angleBetween(lookVec) > getFieldOfView() * 0.5f) return@filter false
                            rayCast(orientation.pos, unit.orientation.pos) { polygon ->
                                polygon != this@GameUnit.polygon &&
                                        polygon != unit.polygon &&
                                        polygon.gameObject?.let {
                                            it is WithPolygon && it.collide
                                        } ?: false
                            } == null
                        }
                )
            }
        }
        return unitsInSight
    }

    private fun updateRotation(sinceMillis: Long) {
        if (rotationTo != orientation.rotation) {
            val multiplier = getSpeed() * sinceMillis * 180f
            val distance = (rotationTo - orientation.rotation).let {
                if (abs(it) > 180f) { it + if (it > 0f) -360f else 360f } else it
            }
            val length = abs(distance)
            orientation.rotation = if (length > multiplier) {
                (orientation.rotation + distance / length * multiplier + 360f) % 360f
            } else rotationTo
        }
        lookVec.set(0f, -1f)
        lookVec.rotate(orientation.rotation)
        lookVec.normalize()
    }

    private fun updateBehavior(sinceMillis: Long) {
        behaviorTickAcc += sinceMillis
        if (behaviorTickAcc > behaviorTickGap) {
            behaviorTickAcc %= behaviorTickGap
            behaviorTickGap = Configuration.getBehaviorTickGap()
            behaviorTick()
        }
    }

    open fun update(sinceMillis: Long) {
        if (isOperative()) {
            updateRotation(sinceMillis)
            updateBehavior(sinceMillis)
            state?.update(sinceMillis)
        }
        val effect = effects.iterator()
        while (effect.hasNext()) {
            effect.next().also {
                it.update(sinceMillis)
                if (it.isEnded()) effect.remove()
            }
        }
    }

    override fun onAdd(parent: Entity) {
        super.onAdd(parent)
        changeState(IdleState(this))
    }

    fun getRelation() = Game.getRelation(organisationId)

    fun getSkirmishRelation() =
            Skirmish.instance.playerController.organisation.getRelation(organisationId)

    fun isSkirmishRelation(relation: Organisation.Relation) = relation == getSkirmishRelation()

    enum class Visibility {
        Visible,
        Sensed,
        Invisible,
    }
}
