package pl.garedgame.game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.subOjects.Production

class ProductionAdapter(private val productions: ArrayList<Production> = arrayListOf()) : RecyclerView.Adapter<TextViewHolder>() {
    private var selected = if (productions.isEmpty()) -1 else 0
    var onClick: (() -> Unit)? = null
    var isCurrent: ((Production) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        return TextViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false))
    }

    fun getSelectedProduction(): Production? = if (selected >= 0 && selected < productions.size) {
        productions[selected]
    } else null

    override fun getItemCount() = productions.size

    fun update(_researches: ArrayList<Production>) {
        selected = if (_researches.isEmpty()) -1 else 0
        productions.clear()
        productions.addAll(ArrayList(_researches))
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val production = productions[position]
        val template = Game.content.templateEquipment(production.item)
        if (isCurrent?.invoke(production) == true)
            holder.name.text = "${template.name} >>"
        else holder.name.text = template.name

        if (selected == position) {
            holder.itemView.setBackgroundResource(R.drawable.primary_framed)
        } else holder.itemView.background = null
        holder.itemView.setBounceOnClick {
            val prevSelected = selected
            selected = if (selected == position) -1 else position
            if (selected >= 0) notifyItemChanged(selected)
            if (prevSelected >= 0) notifyItemChanged(prevSelected)
            onClick?.invoke()
            notifyItemChanged(productions.indexOf(production))
        }
    }
}
