package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import pl.garedgame.game.Configuration
import pl.garedgame.game.render.TextureRegion
import pl.garedgame.game.util.Utils
import kotlin.math.max

class PersonSkills {
    @Expose
    var soldierSkill = Skill()
    @Expose
    var engineerSkill = Skill()
    @Expose
    var mechanicSkill = Skill()
    @Expose
    var scientistSkill = Skill()

    init {
        soldierSkill.XP = Utils.rand.nextLong(1000L)
        engineerSkill.XP = Utils.rand.nextLong(1000L)
        mechanicSkill.XP = Utils.rand.nextLong(1000L)
        scientistSkill.XP = Utils.rand.nextLong(1000L)
        val bestSkill = Skill.max(Skill.max(soldierSkill, engineerSkill), Skill.max(mechanicSkill, scientistSkill))
        bestSkill.XP = Utils.rand.nextLong(2000L)
    }

    fun getSpeed(mod: Float): Float = Configuration.PERSON_SPEED_BASE +
            Configuration.PERSON_SPEED_PER_LEVEL * max(0f, soldierSkill.getLevel().toFloat() + mod)

    fun getRangeOfView(): Float = Configuration.PERSON_ROV_BASE +
            Configuration.PERSON_ROV_PER_LEVEL * soldierSkill.getLevel()
    fun getFieldOfView(): Float = Configuration.PERSON_FOV_BASE +
            Configuration.PERSON_FOV_PER_LEVEL * soldierSkill.getLevel()

    fun maxHealth(): Int = Configuration.PERSON_HEALTH_BASE +
            (soldierSkill.getLevel() * Configuration.PERSON_HEALTH_PER_LEVEL).toInt()

    fun preferProfession(): Profession {
        val bestSkill = Skill.max(
                Skill.max(soldierSkill, engineerSkill),
                Skill.max(mechanicSkill, scientistSkill)
        )
        return when (bestSkill) {
            soldierSkill -> Profession.Soldier
            engineerSkill -> Profession.Engineer
            mechanicSkill -> Profession.Mechanic
            scientistSkill -> Profession.Scientist
            else -> Profession.None
        }
    }
}

enum class Profession(val texture: String) {
    @SerializedName("none") None("none"),
    @SerializedName("soldier") Soldier("soldier"),
    @SerializedName("engineer") Engineer("engineer"),
    @SerializedName("mechanic") Mechanic("mechanic"),
    @SerializedName("scientist") Scientist("scientist");

    fun getTextureRegion(): TextureRegion? = TextureRegion.getRegion(texture)
}
