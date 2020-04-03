package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose

data class Research(
        @Expose val key: String,
        @Expose val name: String,
        @Expose val description: String,
        @Expose val requiredResearch: ArrayList<String>? = null,
        @Expose val cost: Cost
) : Job {
    @Expose
    var progress = 0L

    override fun progress() = progress
    override fun duration() = cost.time ?: 1L
    override fun cost() = cost

    override fun update(pSinceMillis: Long) {
        if (progress < duration()) {
            progress += pSinceMillis
        } else {
            progress = duration()
        }
    }
}
