package pl.garedgame.game.entities

import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.collisions.DiamondPolygon
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.objects.subOjects.Equipment

class ItemObject(
    val equipment: Equipment,
    var onCollision: ((WithPolygon) -> Unit)? = null
) : GameObject(SpaceOrientation2D(0.2f, 0.2f)), WithPolygon {
    override val dynamic: Boolean = true
    override val collide: Boolean = true
    override val polygon: Polygon = DiamondPolygon()
    override fun onCollision(other: WithPolygon) { onCollision?.invoke(other) }
}
