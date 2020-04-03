package pl.garedgame.game.objects

import com.google.gson.annotations.Expose
import java.lang.ref.WeakReference

abstract class Assignation : GameUnit() {

    @Expose
    val units = arrayListOf<GameUnit>()

    fun updateAssignations() {
        for (unit in units) {
            unit.assignation = WeakReference(this)
            if (unit is Assignation) unit.updateAssignations()
        }
    }

    fun getHighestId(): Long {
        var highestId = id
        for (unit in units) {
            val unitId = if (unit is Assignation) unit.getHighestId() else unit.id
            if (unitId > highestId) highestId = unitId
        }
        return highestId
    }

    fun <T : GameUnit>getCountOf(clazz: Class<T>): Int {
        var count = units.filter { it::class.java == clazz }.size
        for (unit in units) {
            if (unit is Assignation) count += unit.getCountOf(clazz)
        }
        return count
    }

    open fun assign(gameUnit: GameUnit?): Boolean {
        gameUnit?.let { unit ->
            unit.assignation?.let { assignation ->
                assignation.get()?.unAssign(unit)
            }
            if (units.add(unit)) {
                unit.assignation = WeakReference(this@Assignation)
                if (unit.organisationId == 0L && organisationId > 0L) {
                    unit.organisationId = organisationId
                }
                return true
            }
        }
        return false
    }

    open fun unAssign(gameUnit: GameUnit): Boolean {
        if (units.remove(gameUnit)) {
            gameUnit.assignation = null
            return true
        }
        return false
    }

    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        for (unit in units) unit.update(sinceMillis)
    }
}
