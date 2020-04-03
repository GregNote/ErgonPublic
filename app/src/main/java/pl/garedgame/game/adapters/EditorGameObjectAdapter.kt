package pl.garedgame.game.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import pl.garedgame.game.R
import pl.garedgame.game.entities.GameObject
import pl.garedgame.game.entities.Terrain
import pl.garedgame.game.extensions.setOnClick

class EditorObjectViewHolder(
        view: View
) : RecyclerViewHolder<GameObject>(view) {

    private val name: TextView = view.findViewById(R.id.name)
    val moveUp: TextView = view.findViewById(R.id.moveUp)
    val moveDown: TextView = view.findViewById(R.id.moveDown)

    override fun bind(item: GameObject?) {
        item?.also {
            moveUp.visibility = if (it is Terrain) View.VISIBLE else View.GONE
            moveDown.visibility = if (it is Terrain) View.VISIBLE else View.GONE
            name.text = "${it::class.java.simpleName}\n${it.orientation.pos}"
        }
    }
    override fun clear() { }
}

class EditorGameObjectAdapter(
        items: ArrayList<GameObject> = arrayListOf()
) : RecyclerAdapter<GameObject, EditorObjectViewHolder>(items) {
    var onMoveUp: ((Int, GameObject) -> Unit)? = null
    var onMoveDown: ((Int, GameObject) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorObjectViewHolder {
        return EditorObjectViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_editor_object,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(viewHolder: EditorObjectViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)
        viewHolder.moveUp.setOnClick { onMoveUp?.invoke(position, items[position]) }
        viewHolder.moveDown.setOnClick { onMoveDown?.invoke(position, items[position]) }
    }
}
