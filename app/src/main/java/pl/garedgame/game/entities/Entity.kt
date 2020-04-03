package pl.garedgame.game.entities

import com.google.gson.annotations.Expose
import java.lang.ref.WeakReference

open class Entity(@Expose var tag: String = "") {

    private var parentWeak = WeakReference<Entity>(null)
    private val children = arrayListOf<Entity>()

    fun getParent(): Entity? = parentWeak.get()

    fun getChildren(): List<Entity> = children.toList()

    open fun clear() {
        children.forEach { it.onRemove(this) }
        children.clear()
    }

    fun contains(entity: Entity) = children.contains(entity)

    open fun add(entity: Entity): Boolean {
        return if (children.add(entity)) {
            entity.onAdd(this)
            true
        } else false
    }

    open fun remove(entity: Entity): Boolean {
        return if (children.remove(entity)) {
            entity.onRemove(this)
            true
        } else false
    }

    open fun onAdd(parent: Entity) {
        parentWeak = WeakReference(parent)
    }

    open fun onRemove(parent: Entity) {
        parentWeak = WeakReference<Entity>(null)
    }
}
