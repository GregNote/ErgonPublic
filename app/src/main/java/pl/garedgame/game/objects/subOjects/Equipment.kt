package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import pl.garedgame.game.Game
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.Vector2
import pl.garedgame.game.entities.ExplosionEffect
import pl.garedgame.game.entities.GameScene
import pl.garedgame.game.entities.ItemObject
import pl.garedgame.game.entities.WithPolygon
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.render.GameViewGL

class WeaponMelee(
        @Expose val minDamage: Int,
        @Expose val maxDamage: Int,
        @Expose val range: Float
) : Equipment() {
    override fun getAbilities(): ArrayList<Ability> = arrayListOf(Ability(Ability.Type.Melee, this))
}

class WeaponRange(
        @Expose val bulletDamage: Int,
        @Expose val bulletCount: Int,
        @Expose val shootCount: Int,
        @Expose val shootDelay: Int,
        @Expose val spreadAngle: Float,
        @Expose val aimTime: Long,
        @Expose val sound: String,
        @Expose val r: Float,
        @Expose val g: Float,
        @Expose val b: Float
) : Equipment() {
    override fun getAbilities(): ArrayList<Ability> = arrayListOf(Ability(Ability.Type.Shoot, this))
    fun shoot(user: GameUnit, from: Vector2, target: Vector2): ArrayList<Pair<Vector2, Vector2>> {
        val result = arrayListOf<Pair<Vector2, Vector2>>()
        user.gameScene?.also { scene ->
            val toTarget = user.orientation.pos.vectorTo(target)
            val left = toTarget.rotated(spreadAngle * -0.5f)
            val right = toTarget.rotated(spreadAngle * 0.5f)
            val range = Vector2.distance(left, right) * 0.5f

            for (i in 0 until bulletCount) {
                val shoot = Vector2.getRandom(target, range)
                val shootAt = scene.rayCast(from, shoot) { polygon ->
                    polygon != user.polygon &&
                            polygon.gameObject?.let {
                                it is WithPolygon && it.collide
                            } ?: false
                }?.let {
                    it.first.gameObject?.also { obj ->
                        if (obj is GameUnit) {
                            obj.takeDamage(bulletDamage)
                        }
                    }
                    it.second
                } ?: shoot
                result.add(from to shootAt)
            }
            if (user is Person) user.skills.soldierSkill.use()
            SoundPlayer.instance.playSound(sound)
        }
        return result
    }
}

class Armor(
        @Expose val armor: Int,
        @Expose val speed: Float
) : Equipment()

class Explosive(
        @Expose val damage: Int,
        @Expose val range: Float,
        @Expose val trigger: ExplosiveTrigger
) : Equipment() {
    override fun getAbilities(): ArrayList<Ability> = arrayListOf(
            Ability(Ability.Type.Throw, this),
            Ability(Ability.Type.Plant, this)
    )
    fun use(user: GameUnit, target: Vector2) {
        user.gameScene?.also { gs ->
            val item = ItemObject(this).apply {
                orientation.pos.set(target)
            }
            if (gs.add(item)) when (trigger) {
                ExplosiveTrigger.Throw -> explode(gs, item, user)
                ExplosiveTrigger.Timer -> GameViewGL.postDelayed({
                    explode(gs, item, user)
                }, 750L)
                ExplosiveTrigger.Mine -> item.onCollision = {
                    explode(gs, item, user)
                }
            }
        }
    }

    private fun explode(gameScene: GameScene, item: ItemObject, user: GameUnit) {
        val inRange = gameScene.unitsInRange(item.orientation.pos, range).filter {
            Vector2.distance(it.orientation.pos, item.orientation.pos) < range
        }
        if (inRange.isNotEmpty() && user is Person) user.skills.soldierSkill.use()
        inRange.forEach { it.takeDamage(damage) }
        gameScene.remove(item)
        user.effects.add(ExplosionEffect(item.orientation.pos, range))
    }
}

class Usable(
        @Expose val loads: Int,
        @Expose val duration: Long,
        @Expose val health: Int? = null
) : Equipment() {
    override fun getAbilities(): ArrayList<Ability> = arrayListOf(Ability(Ability.Type.Use, this))
    fun use(user: GameUnit, target: GameUnit) {
        if (health != null && user is Person && target is Person && target.health > 0) {
            user.skills.scientistSkill.use()
            target.takeHealing(health + user.skills.scientistSkill.getLevel() / 3)
        }
    }
}

class Other : Equipment()

enum class ExplosiveTrigger {
    @SerializedName("throw") Throw,
    @SerializedName("timer") Timer,
    @SerializedName("mine") Mine
}

enum class Slot {
    @SerializedName("weapon") Weapon,
    @SerializedName("armor") Armor,
    @SerializedName("utility") Utility
}

open class Equipment(
        @Expose val key: String = "",
        @Expose val name: String = "",
        @Expose val description: String = "",
        @Expose val slot: Slot? = null,
        @Expose val image: String = "",
        @Expose val requiredResearch: ArrayList<String>? = null,
        @Expose val cost: Cost = Cost()
) {
    companion object {
        fun <T : Equipment>copy(obj: T): T {
            val json = Game.instance.gson.toJson(obj)
            return Game.instance.gson.fromJson(json, obj::class.java)
        }
    }

    open fun getAbilities(): ArrayList<Ability> = arrayListOf()
}
