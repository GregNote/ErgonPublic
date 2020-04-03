package pl.garedgame.game.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.annotations.Expose
import pl.garedgame.game.*
import pl.garedgame.game.adapters.GameUnitViewHolder
import pl.garedgame.game.behavior.*
import pl.garedgame.game.collisions.PentagonPolygon
import pl.garedgame.game.databinding.ListItemPersonBinding
import pl.garedgame.game.entities.BloodEffect
import pl.garedgame.game.entities.Entity
import pl.garedgame.game.entities.HealEffect
import pl.garedgame.game.entities.ShootEffect
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.render.TextureRegion
import pl.garedgame.game.render.TextureSprite
import pl.garedgame.game.util.Utils
import kotlin.math.max
import kotlin.math.min

class Person: GameUnit(SpaceOrientation2D(0.4f, 0.4f), PentagonPolygon()) {

    companion object {
        fun getRandOf(list: List<String>): String = list[Utils.rand.nextInt(list.size)]
    }

    @Expose var isWoman = Utils.rand.nextBoolean()
    @Expose var fName: String
    @Expose var nName: String = ""
    @Expose var lName: String = Game.content.lastNames[Utils.rand.nextInt(Game.content.lastNames.size)]

    @Expose var profession: Profession = Profession.None

    @Expose val appearance = PersonAppearance()
    @Expose val skills = PersonSkills()

    var healthListener:(() -> Unit)? = null
    @Expose var health: Int = maxHealth()
        set(value) {
            field = value
            healthListener?.invoke()
        }

    @Expose var primaryWeapon: Equipment? = null
    @Expose var secondaryWeapon: Equipment? = null
    @Expose var armor: Equipment? = null
    @Expose var utilityItem: Equipment? = null

    val weaponPomVec = Vector2.ZERO
    val weaponAimVec = Vector2.ZERO
    val weaponVec = Vector2.ZERO

    var professionTexture: TextureRegion? = null
    var corpsesTexture: TextureRegion? = null
    var warnTexture: TextureRegion? = null

    init {
        fName = if (isWoman) {
            Game.content.firstNamesFemale[Utils.rand.nextInt(Game.content.firstNamesFemale.size)]
        } else {
            Game.content.firstNamesMale[Utils.rand.nextInt(Game.content.firstNamesMale.size)]
        }
        profession = skills.preferProfession()
        behaviorRoot = DefaultBehaviors.playerPersonDefaultBehaviors()
        behavior = Behavior(this, listOfSensors(), behaviorRoot!!)
        health = maxHealth()
    }

    override val collide: Boolean
        get() = getState() !is DeadState

    override var visible: Visibility = Visibility.Visible
        set(value) {
            if (field != value) when {
                !isOperative() -> corpsesTexture
                value == Visibility.Visible -> professionTexture
                value == Visibility.Sensed -> warnTexture
                else -> null
            }.also { mesh.textureRegion = it ?: TextureRegion.EMPTY }
            field = value
        }

    private val weapon = TextureSprite().also { sprite ->
        sprite.orientation.scaleX = 0.22f
        sprite.orientation.scaleY = 0.11f
        sprite.textureRegion = TextureRegion.EMPTY
    }

    private val bottom = TextureSprite().also { sprite ->
        sprite.orientation.scaleX = 0.4f
        sprite.orientation.scaleY = 0.4f
        sprite.textureRegion = TextureRegion.EMPTY
    }

    override val meshes: ArrayList<TextureSprite> = arrayListOf(bottom, weapon, mesh)

    override fun listOfSensors(): List<Sensor> {
        return listOf(
                IsOperativeSensor(), IsSelectedSensor(), EnemyInSightSensor(),
                WeaponSensor(), OrderSensor(), HasHealingItemSensor(),
                IsWoundedSensor(), IsWoundedSquadMateSensor()
        )
    }

    override fun changeState(unitState: UnitState) {
        super.changeState(unitState)
        if (!isOperative()) updateRelationTexture()
    }

    override fun onAdd(parent: Entity) {
        super.onAdd(parent)
        professionTexture = profession.getTextureRegion()
        corpsesTexture = TextureRegion.getRegion("corpses")
        warnTexture = TextureRegion.getRegion("warn")
        updateWeaponTexture()
        updateRelationTexture()
    }

    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        if (isOperative()) {
            bottom.orientation.pos.set(orientation.pos)
            bottom.orientation.rotation = (bottom.orientation.rotation + sinceMillis * 0.05f) % 360f

            if (getState() is ShootState) {
                weaponPomVec.set(0f, -0.22f)
                weaponPomVec.rotate((orientation.rotation + 320f) % 360f)
                weapon.orientation.rotation = ((weaponAimVec.rotation() + 270f) % 360f)
            } else {
                weaponPomVec.set(0f, -0.12f)
                weaponPomVec.rotate((orientation.rotation + 340f) % 360f)
                weapon.orientation.rotation = (orientation.rotation + 350f) % 360f
            }
            weaponVec.set(orientation.pos)
            weaponVec.plusAssign(weaponPomVec)
            weapon.orientation.pos.set(weaponVec)

        }
    }

    override fun isOperative(): Boolean = getState() !is DeadState
    override fun getSpeed(): Float = skills.getSpeed((armor as Armor?)?.speed ?: 0f)
    override fun getRangeOfView(): Float = skills.getRangeOfView()
    override fun getFieldOfView(): Float = skills.getFieldOfView()
    fun maxHealth(): Int = skills.maxHealth()

    override fun takeHealing(healing: Int) {
        super.takeHealing(healing)
        health = min(health + healing, maxHealth())
        for (i in 1..healing) effects.add(HealEffect(orientation.pos))
    }

    override fun takeDamage(damage: Int) {
        super.takeDamage(damage)
        val armorValue = armor?.let { item ->
            if (item is Armor) item.armor else 0
        } ?: 0
        val takeDamage = max(damage - armorValue, 0)
        if (takeDamage > 0) {
            health = max(health - takeDamage, 0)
            effects.add(BloodEffect(orientation.pos))
            Notification.notify(Notification.Event.DamageTaken)
        }
        if (health == 0) changeState(DeadState(this))
    }

    fun getFullName(): String {
        return if (nName.isEmpty()) "$fName $lName"
        else "$fName \'${nName}\' $lName"
    }

    fun getNickOrName(): String {
        return if (nName.isEmpty())
            "${fName.subSequence(0, 1)}. $lName"
        else
            "'${nName}\'"
    }

    fun swapWeapons() {
        val temp = secondaryWeapon
        secondaryWeapon = primaryWeapon
        primaryWeapon = temp
        updateWeaponTexture()
    }

    private fun updateRelationTexture() {
        if (isOperative()) {
            when (getSkirmishRelation()) {
                Organisation.Relation.Self -> TextureRegion.getRegion("self")?.apply {
                    bottom.textureRegion = this
                }
                Organisation.Relation.Neutral -> TextureRegion.getRegion("neutral")?.apply {
                    bottom.textureRegion = this
                }
                Organisation.Relation.Ally -> TextureRegion.getRegion("ally")?.apply {
                    bottom.textureRegion = this
                }
                Organisation.Relation.Enemy -> TextureRegion.getRegion("enemy")?.apply {
                    bottom.textureRegion = this
                }
            }
        } else {
            bottom.textureRegion = TextureRegion.EMPTY
        }
    }

    private fun updateWeaponTexture() {
        weapon.textureRegion = primaryWeapon?.let {
            if (isOperative()) TextureRegion.getRegion(it.image) else null
        } ?: TextureRegion.EMPTY
    }

    fun getAbilities(): ArrayList<Ability> {
        val abilities = arrayListOf<Ability>()
        if (BuildConfig.DEBUG && (primaryWeapon != null || secondaryWeapon != null)) {
            abilities.add(Ability(Ability.Type.Attack))
            abilities.add(Ability(Ability.Type.Defence))
        }
        abilities.add(Ability(Ability.Type.Recon))
        abilities.add(Ability(Ability.Type.Patrol))
        primaryWeapon?.let { abilities.addAll(it.getAbilities()) }
        armor?.let { abilities.addAll(it.getAbilities()) }
        utilityItem?.let { abilities.addAll(it.getAbilities()) }
        return abilities
    }

    override fun playerOrder(position: Vector2, ability: Ability?): Boolean {
        return if (ability != null) {
            val targetUnit = if (ability.type.require ==Ability.Require.Unit)
                gameScene?.unitAtPosition(position) else null
            when (ability.type) {
                Ability.Type.Shoot -> {
                    ability.equipment?.let {
                        if (targetUnit != null) {
                            changeState(ShootState(this, it, targetUnit).apply {
                                itsOrder = true
                            })
                        } else {
                            changeState(ShootState(this, it, targetPosition = position).apply {
                                itsOrder = true
                            })
                        }
                        true
                    } ?: false
                }
                Ability.Type.Melee -> {
                    true
                }
                Ability.Type.Throw -> {
                    ability.equipment?.let {
                        if (it is Explosive) {
                            changeState(ThrowState(this, it, position).apply {
                                itsOrder = true
                            })
                            true
                        } else false
                    } ?: false
                }
                Ability.Type.Plant -> {
                    ability.equipment?.let {
                        if (it is Explosive) {
                            changeState(PlantState(this, it, position).apply {
                                itsOrder = true
                            })
                            true
                        } else false
                    } ?: false
                }
                Ability.Type.Use -> {
                    targetUnit?.let { target ->
                        val inSight = gameScene?.let { gs ->
                            gs.rayCast(orientation.pos, target.orientation.pos) {
                                it != polygon
                            }?.first == target.polygon
                        } ?: false
                        if (inSight) {
                            var toTarget = orientation.pos.vectorTo(target.orientation.pos)
                            toTarget -= toTarget.normalized() * 0.5f
                            moveTo(orientation.pos + toTarget, object : OnStateEndCallback {
                                override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
                                    if (Vector2.distance(orientation.pos, target.orientation.pos) < 0.5f) {
                                        (ability.equipment as Usable?)?.also { usable ->
                                            changeState(UsingItemState(this@Person, usable, target).apply {
                                                itsOrder = true
                                            })
                                        }
                                    }
                                }
                            }, true)
                        } else {
                            moveTo(Vector2.getRandom(target.orientation.pos, 0.5f), object : OnStateEndCallback {
                                override fun onStateEnd(gameUnit: GameUnit, state: UnitState) {
                                    if (Vector2.distance(orientation.pos, target.orientation.pos) < 0.5f) {
                                        (ability.equipment as Usable?)?.also { usable ->
                                            changeState(UsingItemState(this@Person, usable, target).apply {
                                                itsOrder = true
                                            })
                                        }
                                    }
                                }
                            }, true)
                        }
                        true
                    } ?: false
                }
                Ability.Type.Attack -> {
                    true
                }
                Ability.Type.Defence -> {
                    true
                }
                Ability.Type.Recon -> {
                    changeState(ReconState(this).apply { itsOrder = true})
                    true
                }
                Ability.Type.Patrol -> {
                    changeState(PatrolState(this, position).apply { itsOrder = true })
                    true
                }
            }
        } else {
            moveTo(position, fromUser = true)
            true
        }
    }

    open class PersonViewHolder(
            parent: ViewGroup,
            private val binding: ListItemPersonBinding = ListItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) : GameUnitViewHolder(binding.root) {

        override fun bind(item: GameUnit?) {
            if (item is Person) {
                binding.person = item
                binding.isSmall = isSmall
            } else clear()
        }

        override fun clear() {
            binding.person = null
        }
    }

    class DeadState(
            gameUnit: GameUnit
    ) : UnitState(gameUnit, false) {
        init {
            val randIndex = Utils.rand.nextInt(2)
            SoundPlayer.instance.playSound(
                    when (randIndex) {
                        0 -> R.raw.death_01
                        else -> R.raw.death_02
                    }
            )
            gameUnit.texture = "corpses.png"
        }
    }

    class ShootState(
            gameUnit: GameUnit,
            equipment: Equipment,
            private val targetUnit: GameUnit? = null,
            private val targetPosition: Vector2? = null
    ) : UnitState(gameUnit) {
        private var shootCount = 0
        private var lastShootTime = 0L
        private val rangeWeapon = equipment as WeaponRange

        init {
            targetUnit?.also { gameUnitWR.get()?.lookAt(it) }
            targetPosition?.also { gameUnitWR.get()?.lookAt(it) }
        }

        override fun update(sinceMillis: Long) {
            super.update(sinceMillis)
            gameUnitWR.get()?.apply {
                when (this) {
                    is Person -> {
                        if (playTime <= rangeWeapon.aimTime) {
                            targetUnit?.also { unit ->
                                weaponVec.vectorTo(unit.orientation.pos, weaponAimVec)
                            }
                            targetPosition?.also { position ->
                                weaponVec.vectorTo(position, weaponAimVec)
                            }
                            weaponAimVec.normalize()
                        }
                        if (playTime > rangeWeapon.aimTime) {
                            if (shootCount >= rangeWeapon.shootCount) callEnd()
                            if (shootCount < rangeWeapon.shootCount) {
                                if (playTime - lastShootTime >= rangeWeapon.shootDelay) {
                                    ++shootCount
                                    lastShootTime = playTime

                                    targetUnit?.also { unit ->
                                        rangeWeapon.shoot(this, weaponVec, unit.orientation.pos).forEach {
                                            effects.add(ShootEffect(rangeWeapon, it.first, it.second))
                                        }
                                    }
                                    targetPosition?.also { position ->
                                        rangeWeapon.shoot(this, weaponVec, position).forEach {
                                            effects.add(ShootEffect(rangeWeapon, it.first, it.second))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
