package pl.garedgame.game.entities

import com.google.gson.annotations.Expose
import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.collisions.SquarePolygon

class Obstacle(
        @Expose var type: Type = Type.None
) : GameObject(SpaceOrientation2D()), WithPolygon {

    enum class Type {
        None,
        Low,
        High
    }

    override val dynamic: Boolean = false
    override val collide: Boolean = true
    override val polygon: Polygon = SquarePolygon()
}
