package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import pl.garedgame.game.Game

class Production(
        @Expose val item: String,
        @Expose var count: Int
) : Job {
    @Expose
    var progress = 0L

    override fun progress() = progress
    override fun duration() = (cost().time ?: 1L) * count
    override fun cost() = Game.content.templateEquipment(item).cost

    override fun update(pSinceMillis: Long) {
        if (progress < duration()) {
            progress += pSinceMillis
        } else {
            progress = duration()
        }
    }
}
