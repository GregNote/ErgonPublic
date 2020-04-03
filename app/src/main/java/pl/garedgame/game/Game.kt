package pl.garedgame.game

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import pl.garedgame.game.entities.GameObject
import pl.garedgame.game.entities.GameScene
import pl.garedgame.game.entities.Obstacle
import pl.garedgame.game.entities.Terrain
import pl.garedgame.game.objects.*
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.skirmish.SceneMap
import pl.garedgame.game.util.SharedPrefs
import pl.garedgame.game.util.random

class Game {
    companion object {
        var instance = Game()
        var playerOrganisationId = 1L
        lateinit var content: Content

        fun getOrganisation(id: Long): Organisation {
            return instance.save.organisations.find { it.id == id } as Organisation
        }

        fun getRelation(id: Long) = getRelation(playerOrganisationId, id)

        fun getRelation(id1: Long, id2: Long) = instance.save.getRelation(id1, id2)

        private val unitsSortComparator = Comparator<GameUnit> { o1, o2 ->
            when {
                o1 !is Base && o2 is Base -> 1
                o1 is Base && o2 !is Base -> -1
                else -> 0
            }
        }
    }

    val gson: Gson

    init {
        val gameUnitTypeFactory = RuntimeTypeAdapterFactory
                .of(GameUnit::class.java, "unitType")
                .registerSubtype(Person::class.java, "person")
                .registerSubtype(Vehicle::class.java, "vehicle")
                .registerSubtype(Squad::class.java, "squad")
                .registerSubtype(Base::class.java, "base")
                .registerSubtype(Save::class.java, "save")

        val gameObjectTypeFactory = RuntimeTypeAdapterFactory
                .of(GameObject::class.java, "objectType")
                .registerSubtype(Terrain::class.java, "terrain")
                .registerSubtype(Obstacle::class.java, "obstacle")

        val jobTypeFactory = RuntimeTypeAdapterFactory
                .of(Job::class.java, "jobType")
                .registerSubtype(Research::class.java, "research")
                .registerSubtype(Production::class.java, "production")
                .registerSubtype(BuildingCreation::class.java, "building")
        gson = GsonBuilder()
                .registerTypeAdapterFactory(gameObjectTypeFactory)
                .registerTypeAdapterFactory(gameUnitTypeFactory)
                .registerTypeAdapterFactory(jobTypeFactory)
                .excludeFieldsWithoutExposeAnnotation().create()
    }

    var save = Save()

    private fun restart() {
        save.clear()
        GameUnit.gId = 1L
        Organisation.gId = 1L
    }

    fun findGameUnitById(id: Long, assignation: Assignation = save): GameUnit? {
        for (unit in assignation.units) {
            if (unit.id == id)
                return unit
            else if (unit is Assignation) {
                val found = findGameUnitById(id, unit)
                if (found != null) return found
            }
        }
        return null
    }

    fun newGame(organisationName: String, chosenUnits: ArrayList<GameUnit>) {
        restart()

        val playerOrganisation = Organisation(organisationName)
        save.organisations.add(playerOrganisation)

        val base = Base(true).apply {
            organisationId = playerOrganisation.id
            orientation.pos.set((-3..3).random() * 1000f + 500f, (-3..3).random() * 1000f + 500f)
            for (unit in chosenUnits) assign(unit)
            prefabricats = 2000L
            platinum = 1000L
            titanium = 1000L
            ergon = 100L
            for (i in 1..9) items.add(content.newEquipment("pistol"))
            for (i in 1..6) items.add(content.newEquipment("assault_rifle"))
            for (i in 1..3) items.add(content.newEquipment("kevlar_vest"))
            for (i in 1..3) items.add(content.newEquipment("light_kevlar_armor"))
            for (i in 1..3) items.add(content.newEquipment("heavy_kevlar_armor"))
            for (i in 1..6) items.add(content.newEquipment("frag_granade"))
            for (i in 1..3) items.add(content.newEquipment("medkit"))
        }
        save.assign(base)

        for (i in 1..4) {
            val otherOrganisation = Organisation("Organisation_$i")
            save.organisations.add(otherOrganisation)

            save.assign(Base(true).apply {
                organisationId = otherOrganisation.id
                orientation.pos.set((-3..3).random() * 1000f + 500f, (-3..3).random() * 1000f + 500f)
                for (j in 1..chosenUnits.size) {
                    assign(Person())
                }
                prefabricats = 2000L
                platinum = 1000L
                titanium = 1000L
                ergon = 100L
                for (j in 1..9) items.add(content.newEquipment("pistol"))
                for (j in 1..6) items.add(content.newEquipment("assault_rifle"))
                for (j in 1..3) items.add(content.newEquipment("kevlar_vest"))
                for (j in 1..3) items.add(content.newEquipment("light_kevlar_armor"))
                for (j in 1..3) items.add(content.newEquipment("heavy_kevlar_armor"))
                for (j in 1..6) items.add(content.newEquipment("frag_granade"))
                for (j in 1..3) items.add(content.newEquipment("medkit"))
            })
        }

        for (org in save.organisations) {
            for (otherOrg in save.organisations) {
                if (org != otherOrg) org.setRelation(otherOrg.id, Organisation.Relation.random())
            }
        }

        saveGame()
        save.updateIds()
    }

    fun loadGame() {
        restart()
        val json = loadGameJsonString()
        if (json.isNotEmpty()) {
            save = gson.fromJson(json, Save::class.java)
            save.resumeGameAfterLoad()
        }
    }

    fun saveGame() {
        saveGameJsonString(gson.toJson(save))
    }

    fun hasSavedGame(): Boolean {
        return loadGameJsonString().isNotEmpty()
    }

    fun clearSavedGame() {
        saveGameJsonString("")
    }

    fun loadGameJsonString(): String = SharedPrefs.getString("SAVE_FILE_KEY", "save")

    fun saveGameJsonString(json: String) {
        SharedPrefs.putString("SAVE_FILE_KEY", "save", json)
    }

    fun saveToSceneMap(gameScene: GameScene) {
        val json = gson.toJson(SceneMap(gameScene))
        SharedPrefs.putString("SCENE_FILE_KEY", "save", json)
    }

    fun loadSceneMap(): SceneMap {
        val json = SharedPrefs.getString("SCENE_FILE_KEY", "save")
        return if (json.isNotEmpty()) {
            gson.fromJson(json, SceneMap::class.java)
        } else SceneMap()
    }

    fun getUnits(organisationId: Long = 1L): ArrayList<GameUnit> {
        return ArrayList(save.units.filter { it.organisationId == organisationId }).apply {
            sortWith(unitsSortComparator)
        }
    }

    fun getVisibleUnits(organisationId: Long = 1L): ArrayList<GameUnit> {
        val result = ArrayList(
                save.units.filter {
                    it.organisationId == organisationId || getOrganisation(organisationId).getSector(it.orientation.pos).informationLevel == Sector.InformationLevel.Scanned
                }
        )

        return result.apply {
            sortWith(unitsSortComparator)
        }
    }
}
