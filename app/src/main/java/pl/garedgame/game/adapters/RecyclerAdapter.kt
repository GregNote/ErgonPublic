package pl.garedgame.game.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.extensions.setOnClick
import pl.garedgame.game.extensions.setOnLongClick

abstract class RecyclerViewHolder<E>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: E?)
    open fun onSelected(selected: Boolean) {
        itemView.setBackgroundResource(
                if (selected) R.drawable.primary_dark_framed
                else R.drawable.primary_framed
        )
    }
    abstract fun clear()
}

abstract class RecyclerAdapter<E, Q : RecyclerViewHolder<E>>(_items: ArrayList<E> = arrayListOf()) : RecyclerView.Adapter<Q>() {

    open val items: ArrayList<E> = _items

    var selectable = false
    var selectedIndex = -1
    var onSelected: ((Int, E?) -> Unit)? = null

    var onClick: ((Int, E) -> Unit)? = null
    var onLongClick: ((Int, E) -> Unit)? = null

    private var selected: Q? = null

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onSelected = null
        onClick = null
        onLongClick = null
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(viewHolder: Q, position: Int) {
        val item = items[position]
        if (selectable) {
            viewHolder.itemView.setOnClick {
                if (selectedIndex != position) {
                    if (selectedIndex in items.indices) selected?.onSelected(false)
                    selectedIndex = position
                    selected = viewHolder
                } else {
                    selectedIndex = -1
                    selected = null
                }
                viewHolder.onSelected(selectedIndex == position)
                onSelected?.invoke(selectedIndex, if (selectedIndex in items.indices) items[selectedIndex] else null)
            }
        } else {
            viewHolder.itemView.setBounceOnClick { onClick?.invoke(items.indexOf(item), item) }
        }
        viewHolder.itemView.setOnLongClick {
            onLongClick?.run {
                invoke(items.indexOf(item), item)
                true
            } ?: false
        }
        viewHolder.bind(item)
        viewHolder.onSelected(selectedIndex == position)
    }

    fun addItem(item: E) {
        if (items.add(item)) notifyItemInserted(items.size)
    }

    fun removeItem(item: E): Boolean {
        val index = items.indexOf(item)
        if (index in items.indices && items.remove(item)) {
            notifyItemRemoved(index)
            return true
        }
        return false
    }

    fun addItems(newItems: Collection<E>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateItems(newItems: Collection<E>) {
        items.clear()
        addItems(newItems)
    }

    fun clearSelected() {
        selectedIndex = -1
        notifyDataSetChanged()
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun sortItems(comparator: Comparator<E>) {
        items.sortWith(comparator)
        notifyDataSetChanged()
    }

    fun getSelected(): E? {
        return if (selectable && selectedIndex in 0 until items.size) {
            val result = items[selectedIndex]
            result
        } else null
    }
}
