package pl.garedgame.game.skirmish

import pl.garedgame.game.Game
import pl.garedgame.game.objects.Assignation
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Organisation
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Equipment

open class Controller(organisation: Organisation? = null) {

    companion object {
        fun getSoldier(): Person {
            return arrayListOf<Person>().apply {
                for (i in 0 until 32) add(Person())
            }.maxBy {
                it.skills.soldierSkill.XP + it.skills.scientistSkill.XP / 20
            } as Person
        }
        fun getScientist(): Person {
            return arrayListOf<Person>().apply {
                for (i in 0 until 32) add(Person())
            }.maxBy {
                it.skills.scientistSkill.XP + it.skills.soldierSkill.XP / 20
            } as Person
        }
    }

    var name: String
        get() = organisation.name
        set(value) { organisation.name = value }

    val organisation: Organisation = organisation ?: Organisation()
    var leader = Person()
    val slots = arrayListOf<GameUnit>()
    val items = arrayListOf<Equipment>()

    init {
        prepareForSkirmish()
    }

    fun prepareForSkirmish() {
        slots.clear()
        slots.add(getSoldier().apply {
            leader = this
            fName = name
            nName = "Leader"
            primaryWeapon = Game.content.newEquipment("shotgun")
            secondaryWeapon = Game.content.newEquipment("pistol")
            armor = Game.content.newEquipment("heavy_kevlar_armor")
        })
        for (i in 1..2) slots.add(getSoldier().apply {
            primaryWeapon = Game.content.newEquipment("assault_rifle")
            secondaryWeapon = Game.content.newEquipment("pistol")
            armor = Game.content.newEquipment("light_kevlar_armor")
        })
        slots.add(getScientist().apply {
            primaryWeapon = Game.content.newEquipment("pistol")
            armor = Game.content.newEquipment("kevlar_vest")
            utilityItem = Game.content.newEquipment("medkit")
        })
        slots.forEach { it.organisationId = this.organisation.id }
        items.clear()
        items.apply {
            add(Game.content.newEquipment("assault_rifle"))
            add(Game.content.newEquipment("shotgun"))
            add(Game.content.newEquipment("frag_granade"))
            add(Game.content.newEquipment("tactical_explosive"))
            add(Game.content.newEquipment("mine"))
        }
    }

    fun isAnyAlive(): Boolean = getAllUnits().any { it is Person && it.health > 0 }

    fun getAllUnits(): List<GameUnit> {
        return arrayListOf<GameUnit>().apply {
            slots.forEach {
                if (it !is Assignation) add(it)
                else addAll(getUnitsFromAssignation(it))
            }
        }
    }

    private fun getUnitsFromAssignation(assignation: Assignation): ArrayList<GameUnit> {
        val result = arrayListOf<GameUnit>()
        assignation.units.forEach {
            if (it !is Assignation) result.add(it)
            else result.addAll(getUnitsFromAssignation(it))
        }
        return result
    }
}
