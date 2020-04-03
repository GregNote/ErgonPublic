package pl.garedgame.game.entities

import com.google.gson.annotations.Expose
import pl.garedgame.game.Game
import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.render.TextureRegion
import pl.garedgame.game.render.TextureSprite

open class GameObject(
        @Expose val orientation: SpaceOrientation2D
) : Entity() {

    companion object {
        fun <T : GameObject>copy(obj: T): T {
            val json = Game.instance.gson.toJson(obj)
            return Game.instance.gson.fromJson(json, obj::class.java)
        }
    }

    val mesh = TextureSprite()

    @Expose var texture: String = ""
        set(value) {
            field = value
            updateTextureRegion()
        }

    val gameScene: GameScene?
        get() {
            val parent = getParent()
            return if (parent is GameScene) parent
            else null
        }

    private fun updateTextureRegion() = TextureRegion.getRegion(texture)?.also {
        mesh.textureRegion = it
    }

    override fun onAdd(parent: Entity) {
        super.onAdd(parent)
        mesh.orientation = orientation
        if (this is WithPolygon) {
            polygon.orientation = orientation
            polygon.gameObject = this
        }
        updateTextureRegion()
    }
}

interface WithPolygon {
    val dynamic: Boolean
    val collide: Boolean
    val polygon: Polygon
    fun onCollision(other: WithPolygon) { }
}
