package pl.garedgame.game.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import pl.garedgame.game.R
import pl.garedgame.game.entities.GameObject

class GameClass(
        val name: String,
        private val creator: () -> GameObject
) {
    fun instance(): GameObject = creator()
}

class GameObjectViewHolder(
        view: View
) : RecyclerViewHolder<GameClass>(view) {
    private val name: TextView = view.findViewById(R.id.name)

    override fun bind(item: GameClass?) {
        item?.also { name.text = it.name }
    }
    override fun clear() { }
}

class GameObjectAdapter(
        items: ArrayList<GameClass> = arrayListOf()
) : RecyclerAdapter<GameClass, GameObjectViewHolder>(items) {
    init {
        selectable = true
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameObjectViewHolder {
        return GameObjectViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_object,
                        parent,
                        false
                )
        )
    }
}
