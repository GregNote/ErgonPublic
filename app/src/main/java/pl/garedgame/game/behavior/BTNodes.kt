package pl.garedgame.game.behavior

import com.google.gson.annotations.Expose
import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.skirmish.Skirmish

class BTBoolean(
        @Expose val wsf: WSF,
        @Expose val value: Boolean
) : BTSimpleCondition({ worldState ->
    worldState.blackboard.booleans[wsf] == value
})

class BTRecon : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        if (worldState.gameUnit.getState() !is ReconState) {
            worldState.gameUnit.changeState(ReconState(worldState.gameUnit))
        }
        return Status.Running
    }
}

class BTAttackNearestEnemy : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        if (worldState.gameUnit.getState() !is Person.ShootState) {
            worldState.blackboard.longs[WSF.NearestEnemyInSightId]?.let { nearestEnemyInSightId ->
                worldState.gameUnit.gameScene?.findUnit {
                    it.id == nearestEnemyInSightId
                }?.let { target ->
                    if (worldState.gameUnit is Person && worldState.gameUnit.primaryWeapon != null) {
                        worldState.gameUnit.primaryWeapon?.also {
                            when (it) {
                                is WeaponRange -> worldState.gameUnit.changeState(
                                        Person.ShootState(worldState.gameUnit, it, target))
                            }
                        }
                    }
                    else return Status.Failure
                } ?: return Status.Failure
            } ?: return Status.Failure
        }
        return Status.Running
    }
}

class BTFollowLeader : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        Skirmish.getControllerByOrganisationId(worldState.gameUnit.organisationId)?.also {
            if (worldState.gameUnit == it.leader) return Status.Failure
            val leaderInSight = worldState.gameUnit.unitsInSight().contains(it.leader)
            val leaderIsNear = Vector2.distance(it.leader.orientation.pos, worldState.gameUnit.orientation.pos) < 2f
            if (!leaderInSight) {
                if (leaderIsNear) {
                    worldState.gameUnit.lookAt(it.leader)
                } else {
                    val currSate = worldState.gameUnit.getState()
                    if (!(currSate is MoveState && Vector2.distance(it.leader.orientation.pos, currSate.to) < 2f)) {
                        worldState.gameUnit.moveTo(Vector2.getRandom(it.leader.orientation.pos, 1f))
                    }
                }
            }
        }
        return Status.Running
    }
}

class BTMoveNearToUnit(val unitIdKey: WSF) : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        worldState.blackboard.longs[unitIdKey]?.let { unitId ->
            worldState.gameUnit.gameScene?.findUnit { it.id == unitId }
        }?.also { unit ->
            worldState.gameUnit.gameScene?.also { scene ->
                if (Vector2.distance(unit.orientation.pos, worldState.gameUnit.orientation.pos) < 0.5f) {
                    return Status.Success
                } else {
                    scene.getNode(unit.orientation.pos)?.also { unitNode ->
                        scene.getNeighbours(unitNode).minBy { node ->
                            Vector2.distance(worldState.gameUnit.orientation.pos, node.pos)
                        }?.also { nearestNode ->
                            val state = worldState.gameUnit.getState()
                            if (state is MoveState && Vector2.distance(nearestNode.pos, state.to) <= 0.25f) {
                                return Status.Running
                            }
                            if (state !is MoveState || Vector2.distance(nearestNode.pos, state.to) > 0.25f) {
                                worldState.gameUnit.changeState(
                                        MoveState(worldState.gameUnit, nearestNode.pos)
                                )
                                return Status.Running
                            }
                        }
                    }
                }
            }
        }
        return Status.Failure
    }
}

class BTHealSelf : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        if (worldState.gameUnit.getState() !is UsingItemState) {
            val item = (worldState.gameUnit as Person).utilityItem
            if (item is Usable) {
                worldState.gameUnit.changeState(
                        UsingItemState(worldState.gameUnit, item, worldState.gameUnit)
                )
                return Status.Running
            }
            return Status.Failure
        }
        return Status.Running
    }
}

class BTHealMostWoundedSquadMate : BTNode() {
    override fun update(worldState: WorldState<*>): Status {
        worldState.blackboard.longs[WSF.MostWoundedSquadMateId]?.also { mostWoundedSquadMateId ->
            worldState.gameUnit.gameScene?.findUnit {
                it.id == mostWoundedSquadMateId
            }?.also { target ->
                val item = (worldState.gameUnit as Person).utilityItem
                if (item is Usable) {
                    if (Vector2.distance(worldState.gameUnit.orientation.pos, target.orientation.pos) < 0.5f) {
                        val state = worldState.gameUnit.getState()
                        if (state is UsingItemState && state.target == target) {
                            return Status.Running
                        }
                        if (state !is UsingItemState || state.target != target) {
                            worldState.gameUnit.changeState(
                                    UsingItemState(worldState.gameUnit, item, target)
                            )
                            return Status.Running
                        }
                    }
                }
            }
        }
        return Status.Failure
    }
}
