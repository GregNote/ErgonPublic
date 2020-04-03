package pl.garedgame.game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.garedgame.game.R

class EventAdapter : RecyclerView.Adapter<TextViewHolder>() {

    override fun getItemCount() = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        return TextViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false))
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.name.text = "test event $position >>"
    }
}
