package pl.garedgame.game.objects

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import pl.garedgame.game.Game
import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.subOjects.Research
import pl.garedgame.game.objects.subOjects.Sector
import pl.garedgame.game.util.Utils

class Organisation(@Expose var name: String = "") {

    companion object {
        var gId = 1L
    }

    @Expose
    var id = gId++

    @Expose
    var archivedResearches = arrayListOf<Research>()

    @Expose
    var relationsState = mutableMapOf<Long, Relation>()

    @Expose
    var sectors = arrayListOf<Sector>()

    fun getSector(pos: Vector2): Sector {
        return getSector(Sector.getCoordinate(pos.x), Sector.getCoordinate(pos.y))
    }

    fun getSector(x: Long, y: Long): Sector {
        var sector = sectors.find { it.x == x && it.y == y }
        if (sector == null) {
            sector = Sector.random(x, y)
            sectors.add(sector)
        }
        return sector
    }

    fun setSectorInformationLevel(pos: Vector2, level: Sector.InformationLevel) {
        getSector(Sector.getCoordinate(pos.x), Sector.getCoordinate(pos.y)).informationLevel = level
    }

    fun setRelation(otherId: Long, relation: Relation) {
        relationsState[otherId] = relation
    }

    fun getRelation(otherId: Long): Relation {
        return when {
            id == otherId -> Relation.Self
            relationsState.containsKey(otherId) -> relationsState[otherId] ?: Relation.Neutral
            else -> Relation.Neutral
        }
    }

    fun getAvailableResearch(): ArrayList<Research> {
        val availableResearches = arrayListOf<Research>()
        for (project in Game.content.researches) {
            if (!archivedResearches.any { it.name == project.name }) {
                if (project.requiredResearch == null || project.requiredResearch.size ==
                        project.requiredResearch.filter { req -> archivedResearches.any { arc -> arc.name == req } }.size) {
                    availableResearches.add(project.copy())
                }
            }
        }
        return availableResearches
    }

    fun getAvailableEquipment(): ArrayList<String> {
        val availableEquipment = arrayListOf<String>()
        for (equipment in Game.content.equipment) {
            if (equipment.requiredResearch == null || equipment.requiredResearch.size ==
                    equipment.requiredResearch.filter { req -> archivedResearches.any { arc -> arc.name == req } }.size) {
                availableEquipment.add(equipment.key)
            }
        }
        return availableEquipment
    }

    enum class Relation {
        @SerializedName("self") Self,
        @SerializedName("neutral") Neutral,
        @SerializedName("ally") Ally,
        @SerializedName("enemy") Enemy;

        companion object {
            fun random(): Relation {
                return values()[1 + Utils.rand.nextInt(values().size - 1)]
            }
        }
    }
}
