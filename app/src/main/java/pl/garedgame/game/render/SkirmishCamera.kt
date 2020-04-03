package pl.garedgame.game.render

import android.opengl.Matrix
import pl.garedgame.game.Vector2
import pl.garedgame.game.Vector3
import pl.garedgame.game.util.clamp
import kotlin.math.min

class SkirmishCamera : Camera(Vector3(0f, 0f, (minZ + maxZ) / 2f)) {

    companion object {
        private var minZ = 2f
        private var maxZ = 64f
    }

    private fun screen2sceneVec(x: Float, y: Float): Vector2 {
        val pom = Vector2(x, y)
        pom.divAssign(min(viewportWidth, viewportHeight) * 0.5f)
        pom.timesAssign(pos.z)
        return pom
    }

    private fun screen2scene(x: Float, y: Float): Vector2 {
        val pom = Vector2(x - viewportWidth * 0.5f, y - viewportHeight * 0.5f)
        pom.divAssign(min(viewportWidth, viewportHeight) * 0.5f)
        pom.timesAssign(pos.z)
        return Vector2(
                pos.x + pom.x,
                pos.y - pom.y
        )
    }

    override fun setProjectionMatrix(ratio: Float) {
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, near, far)
    }

    override fun updateMatrix(mvpMatrix: FloatArray) {
        Matrix.setLookAtM(viewMatrix, 0, pos.x, pos.y, pos.z, pos.x, pos.y, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun moveCamera(x: Float, y: Float) {
        screen2sceneVec(x, y).let {
            pos.x -= it.x
            pos.y += it.y
        }
    }

    override fun moveCameraTo(x: Float, y: Float) {
            pos.x = x
            pos.y = y
    }

    override fun getZoom(): Float = pos.z

    override fun setZoom(z: Float) {
        pos.z = clamp(z, minZ, maxZ)
    }

    override fun click(x: Float, y: Float) {
        onClickWithPosition?.invoke(screen2scene(x, y))
    }
}
