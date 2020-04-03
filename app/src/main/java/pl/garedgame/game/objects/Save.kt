package pl.garedgame.game.objects

import com.google.gson.annotations.Expose
import pl.garedgame.game.BuildConfig
import pl.garedgame.game.GameTimer

class Save : Assignation() {
    @Expose
    var version = BuildConfig.VERSION_CODE

    @Expose
    var timer = GameTimer()

    @Expose
    var organisations = arrayListOf<Organisation>()

    override fun clear() {
        organisations.clear()
        units.clear()
        timer = GameTimer()
    }

    fun getRelation(id1: Long, id2: Long): Organisation.Relation {
        val organisation = organisations.find { it.id == id1 }
        return organisation?.getRelation(id2) ?: Organisation.Relation.Neutral
    }

    fun updateIds() {
        gId = getHighestId() + 1L
        var organisationsId = 0L
        for (org in organisations) {
            if (org.id > organisationsId) organisationsId = org.id
        }
        Organisation.gId = organisationsId + 1L
    }

    fun resumeGameAfterLoad() {
        updateAssignations()
        updateIds()
    }
}
