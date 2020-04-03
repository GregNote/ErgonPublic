package pl.garedgame.game.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.annotations.Expose
import pl.garedgame.game.adapters.GameUnitViewHolder
import pl.garedgame.game.databinding.ListItemVehicleBinding
import kotlin.math.min

class Vehicle(@Expose var name: String) : Assignation() {

    var driverId: Long? = null

    fun maxEfficiency() = 30
    override fun getSpeed() = 0.054f
    override fun getRangeOfView() = 1000f

    @Expose
    var efficiency = maxEfficiency()

    fun armor() = 10

    fun getDriver(): Person? {
        driverId?.run {
            return units.find { it.id == this } as Person?
        }
        return null
    }

    fun assignDriver(person: Person) {
        assign(person)
        driverId = person.id
    }

    fun clearDriver() {
        driverId = null
    }

    override fun takeDamage(damage: Int) {
        super.takeDamage(damage)
        efficiency =- min(damage - armor(), efficiency)
    }

    override fun assign(gameUnit: GameUnit?): Boolean {
        return super.assign(gameUnit).also {
            if (it && driverId == null && gameUnit is Person) driverId = gameUnit.id
        }
    }

    override fun unAssign(gameUnit: GameUnit): Boolean {
        return super.unAssign(gameUnit).also {
            if (it && gameUnit.id == driverId) driverId = null
        }
    }

    class VehicleViewHolder(
            parent: ViewGroup,
            private val binding: ListItemVehicleBinding = ListItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) : GameUnitViewHolder(binding.root) {

        override fun bind(item: GameUnit?) {
            if (item is Vehicle) {
                binding.vehicle = item
                binding.isSmall = isSmall
            } else clear()
        }

        override fun clear() {
            binding.vehicle = null
        }
    }
}
