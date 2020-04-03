package pl.garedgame.game.behavior

import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.Organisation
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Usable
import pl.garedgame.game.skirmish.Skirmish

class EnemyInSightSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        worldState.blackboard.booleans.remove(WSF.EnemyInSight)
        worldState.blackboard.integers.remove(WSF.EnemiesInSightCount)
        worldState.blackboard.longs.remove(WSF.NearestEnemyInSightId)
        worldState.gameUnit.apply {
            Skirmish.getControllerByOrganisationId(worldState.gameUnit.organisationId)?.organisation?.also { org ->
                val unitsInSight = unitsInSight()
                val enemies = unitsInSight.filterIsInstance<Person>().filter {
                    org.getRelation(it.organisationId) == Organisation.Relation.Enemy &&
                            it.getState() !is Person.DeadState
                }
                worldState.blackboard.booleans[WSF.EnemyInSight] = enemies.isNotEmpty()
                if (enemies.isNotEmpty()) {
                    worldState.blackboard.integers[WSF.EnemiesInSightCount] = enemies.size
                    enemies.minBy { Vector2.distance(worldState.gameUnit.orientation.pos, it.orientation.pos) }.also { nearest ->
                        nearest?.also {
                            worldState.blackboard.longs[WSF.NearestEnemyInSightId] = it.id
                        }
                    }
                } else {
                    worldState.blackboard.integers[WSF.EnemiesInSightCount] = 0
                    worldState.blackboard.longs[WSF.NearestEnemyInSightId] = -1L
                }
            }
        }
    }
}

class IsSelectedSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        worldState.blackboard.booleans[WSF.IsSelected] = worldState.gameUnit.isSelected()
    }
}

class IsOperativeSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        worldState.blackboard.booleans[WSF.IsOperative] = worldState.gameUnit.isOperative()
    }
}

class WeaponSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        (worldState.gameUnit as Person).also {
            worldState.blackboard.booleans[WSF.HasWeapon] = it.primaryWeapon != null
        }
    }
}

class HasHealingItemSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        (worldState.gameUnit as Person).also { person ->
            val item = person.utilityItem
            if (item is Usable) {
                item.health?.also {
                    worldState.blackboard.booleans[WSF.HasHealingItem] = it > 0
                }
            }
        }
    }
}

class IsWoundedSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        (worldState.gameUnit as Person).also { person ->
            worldState.blackboard.booleans[WSF.IsWounded] = person.health < person.maxHealth()
        }
    }
}

class IsWoundedSquadMateSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        Skirmish.getControllerByOrganisationId(worldState.gameUnit.organisationId)?.also { controller ->
            val woundeds = arrayListOf<Person>()
            controller.slots.forEach { person ->
                if (person is Person && person.health > 0 && person.health < person.maxHealth()) {
                    woundeds.add(person)
                }
            }
            if (woundeds.isNotEmpty()) {
                woundeds.sortedByDescending { it.health }
                worldState.blackboard.longs[WSF.MostWoundedSquadMateId] = woundeds[0].id
                worldState.blackboard.booleans[WSF.IsWoundedSquadMate] = true
            } else {
                worldState.blackboard.longs[WSF.MostWoundedSquadMateId] = -1L
                worldState.blackboard.booleans[WSF.IsWoundedSquadMate] = false
            }
        }
    }
}

class OrderSensor : Sensor {
    override fun sense(worldState: WorldState<*>) {
        worldState.blackboard.booleans[WSF.HasOrder] = worldState.gameUnit.getState().itsOrder
    }
}