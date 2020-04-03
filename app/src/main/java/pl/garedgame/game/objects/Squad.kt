package pl.garedgame.game.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.annotations.Expose
import pl.garedgame.game.Vector2
import pl.garedgame.game.adapters.GameUnitViewHolder
import pl.garedgame.game.databinding.ListItemSquadBinding
import pl.garedgame.game.objects.subOjects.Ability
import pl.garedgame.game.objects.subOjects.Profession

class Squad(
        @Expose var name: String = ""
) : Assignation() {

    init {
        if (name.isEmpty()) name = "S_$id"
        texture = "base.png"
    }

    override fun getSpeed(): Float {
        var result = 0f
        for (unit in units) {
            if (result == 0f || result > unit.getSpeed()) {
                result = unit.getSpeed()
            }
        }
        return result
    }

    override fun getRangeOfView(): Float {
        var result = 0f
        for (unit in units) {
            if (result == 0f || result < unit.getRangeOfView()) {
                result = unit.getRangeOfView()
            }
        }
        return result
    }

    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        orientation.pos.set(0f, 0f)
        units.forEach {
            orientation.pos += it.orientation.pos
        }
        orientation.pos /= units.size.toFloat()
    }

    override fun playerOrder(position: Vector2, ability: Ability?): Boolean {
        return if (ability != null) {
            units.forEach { it.playerOrder(position, ability) }
            true
        } else {
            if (units.isNotEmpty()) {
                var index = 0
                var r = 0.25f
                while (index != units.size) {
                    if (units[index].moveTo(Vector2.getRandom(position, r), fromUser = true)) {
                        ++index
                        r = 0.25f
                    } else {
                        r += 0.05f
                    }
                }
            }
            true
        }
    }

    fun getAbilities(): ArrayList<Ability> {
        val abilities = arrayListOf<Ability>()
        abilities.add(Ability(Ability.Type.Attack))
        abilities.add(Ability(Ability.Type.Defence))
        abilities.add(Ability(Ability.Type.Recon))
        return abilities
    }

    fun getProfessionCount(profession: Profession): String {
        val count = units.count { it is Person && it.profession == profession }
        return if (count > 0) "$count" else "-"
    }

    class SquadViewHolder(
            parent: ViewGroup,
            private val binding: ListItemSquadBinding = ListItemSquadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) : GameUnitViewHolder(binding.root) {

        override fun bind(item: GameUnit?) {
            if (item is Squad) {
                binding.squad = item
                binding.isSmall = isSmall
            } else clear()
        }

        override fun clear() {
            binding.squad = null
        }
    }
}
