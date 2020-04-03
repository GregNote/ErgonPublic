package pl.garedgame.game.render

import pl.garedgame.game.Vector2
import pl.garedgame.game.Vector3
import pl.garedgame.game.objects.GameUnit

abstract class Camera(
        protected var pos: Vector3 = Vector3()
) {
    protected var near = 1f
    protected var far = 6000f
    protected var viewportWidth = 0f
    protected var viewportHeight = 0f

    var onClickWithPosition: ((Vector2) -> Unit)? = null

    protected val projectionMatrix = FloatArray(16)
    protected val viewMatrix = FloatArray(16)
    protected var lockedUnit: GameUnit? = null

    open fun updateProjectionMatrix(width: Int, height: Int) {
        viewportWidth = width.toFloat()
        viewportHeight = height.toFloat()
        setProjectionMatrix(viewportWidth / viewportHeight)
    }

    fun lockTo(gameUnit: GameUnit?) {
        lockedUnit = gameUnit
    }

    abstract fun setProjectionMatrix(ratio: Float)
    abstract fun updateMatrix(mvpMatrix: FloatArray)
    abstract fun moveCamera(x: Float, y: Float)
    abstract fun moveCameraTo(x: Float, y: Float)
    abstract fun getZoom(): Float
    abstract fun setZoom(z: Float)
    abstract fun click(x: Float, y: Float)
}
