package pl.garedgame.game.behavior

import com.google.gson.annotations.Expose
import pl.garedgame.game.objects.GameUnit

enum class WSF {
    EnemyInSight,
    EnemiesInSightCount,
    NearestEnemyInSightId,
    HasWeapon,
    HasOrder,
    HasHealingItem,
    IsSelected,
    IsOperative,
    IsWounded,
    IsWoundedSquadMate,
    MostWoundedSquadMateId
}

class Behavior<E : GameUnit>(gameUnit: E, private val sensors: List<Sensor>, var root: BTNode) {
    private val worldState = WorldState(gameUnit)
    fun tick() {
        for (sensor in sensors) sensor.sense(worldState)
        root.tick(worldState)
    }
}

interface Sensor {
    fun sense(worldState: WorldState<*>)
}

class Board {
    val longs = mutableMapOf<WSF, Long>()
    val floats = mutableMapOf<WSF, Float>()
    val integers = mutableMapOf<WSF, Int>()
    val booleans = mutableMapOf<WSF, Boolean>()
    val strings = mutableMapOf<WSF, String>()
}

class WorldState<E : GameUnit>(val gameUnit: E) {
    val blackboard = Board()
    val whiteboard = Board()
}

abstract class BTNode {
    var status: Status = Status.Invalid
        private set
    open fun abort() {}
    open fun onInitialize() {}
    protected abstract fun update(worldState: WorldState<*>): Status
    open fun onTerminate(status: Status) {}
    fun isRunning(): Boolean = status == Status.Running
    fun isTerminated(): Boolean = status == Status.Success || status == Status.Failure
    fun tick(worldState: WorldState<*>): Status {
        if (status != Status.Running) onInitialize()
        status = update(worldState)
        if (status != Status.Running) onTerminate(status)
        return status
    }
}

abstract class BTComposite(
        vararg list: BTNode
) : BTNode() {
    @Expose
    protected val children = arrayListOf<BTNode>()
    init {
        for (child in list) addChild(child)
    }
    private fun addChild(node: BTNode) = children.add(node)
}

open class BTSequence(
        vararg list: BTNode
) : BTComposite(*list) {
    override fun update(worldState: WorldState<*>): Status {
        for (child in children) {
            val s = child.tick(worldState)
            if (s != Status.Success) return s
        }
        return Status.Success
    }
}

open class BTSimpleCondition(private val condition: (WorldState<*>) -> Boolean) : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        return if (condition(worldState)) Status.Success else Status.Failure
    }
}

class BTFilter(
        @Expose private val condition: BTNode,
        @Expose private val action: BTNode
) : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        val s = condition.tick(worldState)
        return if (s != Status.Success) s else action.tick(worldState)
    }
}

class BTSelector(
        vararg list: BTNode
) : BTComposite(*list) {
    override fun update(worldState: WorldState<*>): Status {
        for (child in children) {
            val s = child.tick(worldState)
            if (s != Status.Failure) return s
        }
        return Status.Failure
    }
}

open class BTParallel(
        @Expose private val successPolicy: Policy,
        @Expose private val failurePolicy: Policy,
        vararg list: BTNode
) : BTComposite(*list) {
    override fun update(worldState: WorldState<*>): Status {
        var successCount = 0
        var failureCount = 0
        for (child in children) {
            if (!child.isTerminated()) child.tick(worldState)
            if (child.status == Status.Success) {
                ++successCount
                if (successPolicy == Policy.RequireOne) return Status.Success
            }
            if (child.status == Status.Failure) {
                ++failureCount
                if (failurePolicy == Policy.RequireOne) return Status.Failure
            }
        }
        if (failurePolicy == Policy.RequireAll && failureCount == children.size) return Status.Failure
        if (successPolicy == Policy.RequireAll && successCount == children.size) return Status.Success
        return Status.Running
    }

    override fun onTerminate(status: Status) {
        for (child in children) {
            if (child.isRunning()) child.abort()
        }
    }
}

enum class Policy {
    RequireOne,
    RequireAll
}

enum class Status {
    Invalid,
    Running,
    Success,
    Failure
}
