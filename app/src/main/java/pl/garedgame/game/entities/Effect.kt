package pl.garedgame.game.entities

import pl.garedgame.game.Vector2
import pl.garedgame.game.objects.subOjects.WeaponRange
import pl.garedgame.game.render.ColorSprite
import pl.garedgame.game.util.Utils
import kotlin.math.min

abstract class Effect(val duration: Long) {
    private var playTime = 0L
    fun percentTime(): Float {
        return playTime.toFloat() / duration.toFloat()
    }
    fun isDuring(): Boolean {
        return playTime < duration
    }
    fun isEnded(): Boolean {
        return playTime >= duration
    }
    open fun update(sinceMillis: Long) {
        playTime += sinceMillis
    }
    abstract fun draw(): ColorSprite
}

class ShootEffect(
        gunType: WeaponRange,
        source: Vector2,
        target: Vector2
) : Effect(200L) {
    private val r = gunType.r * (0.75f + 0.25f * Utils.rand.nextFloat())
    private val g = gunType.g * (0.75f + 0.25f * Utils.rand.nextFloat())
    private val b = gunType.b * (0.75f + 0.25f * Utils.rand.nextFloat())
    private val sprite: ColorSprite

    init {
        val vecTo = source.vectorTo(target).apply { normalize() }
        sprite = ColorSprite()
        sprite.orientation.pos.set(
                (source.x + target.x) / 2f,
                (source.y + target.y) / 2f
        )
        sprite.orientation.rotation = if (vecTo.y > 0f) {
            vecTo.angleBetween(Vector2.X)
        } else {
            -vecTo.angleBetween(Vector2.X)
        }
        sprite.orientation.scaleX = Vector2.distance(source, target)
        sprite.orientation.scaleY = 0.016f
    }

    override fun draw(): ColorSprite {
        if (isDuring()) sprite.changeColor(r, g, b, 1f - percentTime())
        else sprite.changeColor(0f, 0f, 0f, 0f)
        return sprite
    }
}

class HealEffect(
        pos: Vector2
) : Effect(300L) {
    private val baseRotation = Utils.rand.nextFloat()
    private val sprite = ColorSprite().apply {
        orientation.pos.set(Vector2.getRandom(pos, 0.2f))
        orientation.scaleX = 0.075f
        orientation.scaleY = 0.075f
    }
    override fun draw(): ColorSprite {
        if (isDuring()) {
            sprite.orientation.rotation = (baseRotation + percentTime()) * 360f
            sprite.changeColor(0f, 1f, 0f, 0.7f - 0.7f * percentTime())
        } else sprite.changeColor(0f, 0f, 0f, 0f)
        return sprite
    }
}

class BloodEffect(
        pos: Vector2
) : Effect(300L) {
    private val baseRotation = Utils.rand.nextFloat()
    private val sprite = ColorSprite().apply {
        orientation.pos.set(Vector2.getRandom(pos, 0.2f))
        orientation.scaleX = 0.075f
        orientation.scaleY = 0.075f
    }
    override fun draw(): ColorSprite {
        if (isDuring()) {
            sprite.orientation.rotation = (baseRotation + percentTime()) * 360f
            sprite.changeColor(1f, 0f, 0f, 0.7f - 0.7f * percentTime())
        } else sprite.changeColor(0f, 0f, 0f, 0f)
        return sprite
    }
}

class ExplosionEffect(
        pos: Vector2,
        val range: Float
) : Effect(800L) {
    private val baseRotation = Utils.rand.nextFloat()
    private val sprite = ColorSprite().apply {
        orientation.pos.set(pos)
        orientation.scaleX = 0f
        orientation.scaleY = 0f
    }
    override fun draw(): ColorSprite {
        if (isDuring()) {
            sprite.orientation.rotation = (baseRotation + percentTime()) * 720f
            sprite.orientation.scaleX = range * min(1f, percentTime() * 4f)
            sprite.orientation.scaleY = range * min(1f, percentTime() * 4f)
            sprite.changeColor(
                    1f,
                    1f - percentTime(),
                    0f,
                    1f - percentTime())
        } else sprite.changeColor(0f, 0f, 0f, 0f)
        return sprite
    }
}
