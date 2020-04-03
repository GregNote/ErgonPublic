package pl.garedgame.game.render

import android.opengl.Matrix
import pl.garedgame.game.util.clamp

class GlobeCamera : Camera() {
    private var rotAxisX = 0f
    private var rotAxisY = 0f
    private var zoom = 0f

    private fun clampRotation() {
        rotAxisX = clamp(rotAxisX, -90f, 90f)
        if (rotAxisY !in -360f..360f) rotAxisY %= 360f
    }

    override fun setProjectionMatrix(ratio: Float) {
        moveCameraTo(51f, 22f)
        setZoom(170f)
        Matrix.perspectiveM(projectionMatrix, 0, 75f, ratio, near, far)
    }

    override fun updateMatrix(mvpMatrix: FloatArray) {
        pos.set(0f, 0f, 1f)
        pos.rotateX(rotAxisX)
        pos.rotateY(rotAxisY)
        pos.timesAssign(zoom)
        Matrix.setLookAtM(viewMatrix, 0, pos.x, pos.y, pos.z, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun moveCamera(x: Float, y: Float) {
        rotAxisY -= x * 0.2f * ((zoom - 100f) / 100f)
        rotAxisX -= y * 0.2f * ((zoom - 100f) / 100f)
        clampRotation()
    }

    override fun moveCameraTo(x: Float, y: Float) {
        rotAxisX = -x
        rotAxisY = y
        clampRotation()
    }

    override fun getZoom(): Float = zoom

    override fun setZoom(z: Float) {
        zoom = clamp(z, 110f, 190f)
    }

    override fun click(x: Float, y: Float) {
    }
}
