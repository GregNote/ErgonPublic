package pl.garedgame.game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.subOjects.Research

class ResearchAdapter(private val researches: ArrayList<Research> = arrayListOf()) : RecyclerView.Adapter<TextViewHolder>() {
    private var selected = if (researches.isEmpty()) -1 else 0
    var onClick: (() -> Unit)? = null
    var isCurrent: ((Research) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        return TextViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false))
    }

    fun getSelectedResearch(): Research? = if (selected >= 0 && selected < researches.size) {
        researches[selected]
    } else null

    override fun getItemCount() = researches.size

    fun update(_researches: ArrayList<Research>) {
        selected = if (_researches.isEmpty()) -1 else 0
        researches.clear()
        researches.addAll(ArrayList(_researches))
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val project = researches[position]
        if (isCurrent?.invoke(project) == true)
            holder.name.text = "${project.name} >>"
        else holder.name.text = project.name

        if (selected == position) {
            holder.itemView.setBackgroundResource(R.drawable.primary_framed)
        } else holder.itemView.background = null
        holder.itemView.setBounceOnClick {
            val prevSelected = selected
            selected = if (selected == position) -1 else position
            if (selected >= 0) notifyItemChanged(selected)
            if (prevSelected >= 0) notifyItemChanged(prevSelected)
            onClick?.invoke()
            notifyItemChanged(researches.indexOf(project))
        }
    }
}