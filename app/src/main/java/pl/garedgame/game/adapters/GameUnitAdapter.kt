package pl.garedgame.game.adapters

import android.view.View
import android.view.ViewGroup
import pl.garedgame.game.R
import pl.garedgame.game.objects.*

abstract class GameUnitViewHolder(view: View) : RecyclerViewHolder<GameUnit>(view) {
    var isSmall: Boolean = false

    override fun onSelected(selected: Boolean) {
        if (isSmall) {
            if (selected) itemView.setBackgroundResource(R.drawable.primary_framed)
            else itemView.background = null
        } else super.onSelected(selected)
    }
}

class GameUnitAdapter(_items: ArrayList<GameUnit> = arrayListOf())
    : RecyclerAdapter<GameUnit, GameUnitViewHolder>(_items) {

    var isSmall = false

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Person -> 1
            is Vehicle -> 2
            is Squad -> 3
            is Base -> 4
            else -> -1
        }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameUnitViewHolder {
        when (viewType) {
            1 -> return Person.PersonViewHolder(parent).also { it.isSmall = isSmall }
            2 -> return Vehicle.VehicleViewHolder(parent).also { it.isSmall = isSmall }
            3 -> return Squad.SquadViewHolder(parent).also { it.isSmall = isSmall }
            4 -> return Base.BaseViewHolder(parent).also { it.isSmall = isSmall }
        }
        throw RuntimeException("no ViewHolder founded for viewType$viewType")
    }
}
