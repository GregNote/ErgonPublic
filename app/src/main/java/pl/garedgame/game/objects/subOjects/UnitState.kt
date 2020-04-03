package pl.garedgame.game.objects.subOjects

import pl.garedgame.game.Configuration
import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.pathfinding.Node
import pl.garedgame.game.pathfinding.PathFinding
import pl.garedgame.game.render.GameViewGL
import pl.garedgame.game.util.Utils
import java.lang.ref.WeakReference

open class UnitState(
        gameUnit: GameUnit,
        val stopable: Boolean = true,
        val duration: Long = -1L,
        private val onEndCallback: OnStateEndCallback? = null
) {
    var itsOrder = false
    protected var gameUnitWR = WeakReference(gameUnit)
    var playTime = 0L
        private set

    fun callEnd() {
        gameUnitWR.get()?.also {
            it.stop()
            GameViewGL.post {
                onEndCallback?.onStateEnd(it, this)
            }
        }
    }

    open fun abort() { }

    open fun update(sinceMillis: Long) {
        playTime += sinceMillis
        if (duration != -1L && playTime > duration) callEnd()
    }
}

class IdleState(
        gameUnit: GameUnit
) : UnitState(gameUnit) {
    private var nextRotation = Configuration.getBehaviorTickGap() * 30L
    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        gameUnitWR.get()?.apply {
            if (playTime > nextRotation) {
                if (rotationTo == orientation.rotation) {
                    rotationTo = Utils.rand.nextFloat() * 360f
                }
                nextRotation = playTime + Configuration.getBehaviorTickGap() * 10L
            }
        }
    }
}

class WaitState(
        gameUnit: GameUnit,
        duration: Long = 900L,
        onEndCallback: OnStateEndCallback? = null
) : UnitState(gameUnit, duration = duration, onEndCallback = onEndCallback)

open class MovingState(
        gameUnit: GameUnit,
        var to: Vector2,
        onEndCallback: OnStateEndCallback? = null
) : UnitState(gameUnit, onEndCallback = onEndCallback) {

    private val currentRequest: PathFinding.Request?
    var path: ArrayList<Vector2> = arrayListOf()

    init {
        currentRequest = gameUnit.gameScene?.findPath(gameUnit, to) {
            path = it
            if (path.isEmpty()) GameViewGL.postDelayed({
                callEnd()
            }, 300L)
        }
    }

    override fun abort() {
        currentRequest?.aborted = true
    }

    private fun checkPointReached() {
        path.removeAt(0)
        if (path.isEmpty()) {
            callEnd()
        }
    }

    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        gameUnitWR.get()?.apply {
            if (path.isNotEmpty()) path.first().let {
                lookAt(it)
                if (it != orientation.pos) {
                    val distance = it - orientation.pos
                    val length = distance.length()
                    val multiplier = getSpeed() * sinceMillis
                    if (length > multiplier) {
                        orientation.pos += distance / length * multiplier
                    } else {
                        orientation.pos.set(it)
                        checkPointReached()
                    }
                } else {
                    checkPointReached()
                }
            }
        }
    }
}

class MoveState(
        gameUnit: GameUnit,
        to: Vector2,
        onEndCallback: OnStateEndCallback? = null
) : MovingState(gameUnit, to, onEndCallback)

class PatrolState(
        gameUnit: GameUnit,
        patrolTo: Vector2
) : MovingState(gameUnit, patrolTo, object : OnStateEndCallback {
    override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
        if (state is PatrolState) {
            gameUnit.changeState(PatrolState(gameUnit, state.patrolFrom).apply {
                itsOrder = state.itsOrder
            })
        }
    }
}) {
    private val patrolFrom = Vector2(gameUnit.orientation.pos)
}

class UsingItemState(
        gameUnit: GameUnit,
        usable: Usable,
        val target: GameUnit
) : UnitState(gameUnit, duration = usable.duration, onEndCallback = object : OnStateEndCallback {
    override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
        gameUnit.apply {
            if (Vector2.distance(gameUnit.orientation.pos, target.orientation.pos) <= 0.5f) {
                usable.use(gameUnit, target)
            }
        }
    }
}) {
    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        gameUnitWR.get()?.apply {
            lookAt(target.orientation.pos)
            if (Vector2.distance(orientation.pos, target.orientation.pos) > 0.5f) {
                callEnd()
            }
        }
    }
}

class ThrowState(
        gameUnit: GameUnit,
        explosive: Explosive,
        target: Vector2
) : UnitState(gameUnit, duration = 300L, onEndCallback = object : OnStateEndCallback {
    override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
        explosive.use(gameUnit, target)
        if (gameUnit is Person) {
            gameUnit.utilityItem = null
        }
    }
}) {
    init {
        gameUnitWR.get()?.lookAt(target)
    }
}

class PlantState(
        gameUnit: GameUnit,
        explosive: Explosive,
        target: Vector2
) : UnitState(gameUnit, duration = 300L, onEndCallback = object : OnStateEndCallback {
    override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
        if (Vector2.distance(gameUnit.orientation.pos, target) < 0.5f) {
            explosive.use(gameUnit, target)
            if (gameUnit is Person) {
                gameUnit.utilityItem = null
            }
        }
    }
}) {
    init {
        if (Vector2.distance(gameUnit.orientation.pos, target) < 0.5f) {
            gameUnitWR.get()?.lookAt(target)
        } else {
            GameViewGL.post { callEnd() }
        }
    }
}

class ReconState(
        gameUnit: GameUnit
) : MovingState(gameUnit, rand(gameUnit), object : OnStateEndCallback {
    override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
        if (state is ReconState) {
            gameUnit.changeState(WaitState(gameUnit, onEndCallback = object : OnStateEndCallback {
                override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
                    gameUnit.changeState(ReconState(gameUnit).apply {
                        itsOrder = state.itsOrder
                    })
                }
            }).apply {
                itsOrder = state.itsOrder
            })
        }
    }
}) {
    companion object {
        private fun rand(gameUnit: GameUnit): Vector2 {
            gameUnit.gameScene?.let { gs ->
                var result: Node? = gs.getNode(gameUnit.orientation.pos)
                result?.also {
                    for (i in 1..64) {
                        val neighbours = gs.getNeighbours(result!!)
                        val randNeighbour = neighbours[Utils.rand.nextInt(neighbours.size)]
                        if (randNeighbour.walkable &&
                                Vector2.distance(gameUnit.orientation.pos, randNeighbour.pos) >
                                Vector2.distance(gameUnit.orientation.pos, result!!.pos)) {
                            result = randNeighbour
                        }
                    }
                }
                result?.let { if (it.walkable) it.pos else null }
            }?.also {
                return Vector2.getRandom(it, 0.1f)
            }
            return Vector2(gameUnit.orientation.pos)
        }
    }
}

interface OnStateEndCallback {
    fun onStateEnd(gameUnit: GameUnit, state: UnitState)
}
