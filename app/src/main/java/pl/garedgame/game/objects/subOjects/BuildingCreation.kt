package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose

class BuildingCreation(
        @Expose val index: Int,
        @Expose val building: Building
) : Job {
    @Expose
    var progress = 0L

    override fun progress() = progress
    override fun duration() = building.cost.time ?: 1L
    override fun cost() = building.cost

    override fun update(pSinceMillis: Long) {
        if (progress < duration()) {
            progress += pSinceMillis
        } else {
            progress = duration()
        }
    }
}
