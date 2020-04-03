package pl.garedgame.game.render

import android.opengl.GLES30
import pl.garedgame.game.Vector2
import pl.garedgame.game.Vector3
import pl.garedgame.game.util.BYTES_PER_FLOAT

class GlobeMesh(
        private val S: Float,
        private val step: Int = 2,
        private val wireframe: Boolean = false
) : Mesh() {
    private val xFrom = -180 / step
    private val xTo = 180 / step
    private val yFrom = -90 / step
    private val yTo = 90 / step
    override val shaderProgram: ShaderProgram =
            if (wireframe) TextureShader.instance else GlobeShader.instance
    override val vSize: Int = 5
    override val vMaxCount: Int = (xTo - xFrom) * (yTo - yFrom) * 6
    override val static: Boolean = true

    private val pos3D = Vector3()

    init {
        needUpdate = true
    }

    fun setCoordinates(latitude: Float, longitude: Float, h: Float = 0f) {
        pos3D.set(0f, 0f, 1f)
        pos3D.rotateX(-latitude)
        pos3D.rotateY(longitude)
        pos3D.timesAssign(100f + h)
    }

    private fun vec(x: Int, y: Int): Vector3 {
        val vr = Vector3(0f, 0f, -S)
        vr.rotateX(y * step.toFloat())
        vr.rotateY(x * step.toFloat() + 180f)
        return vr
    }

    private val xyz = arrayListOf<Vector3>().apply {
        for (y in yFrom until yTo) {
            for (x in xFrom until xTo) {
                add(vec(x, y))
                add(vec(x + 1, y))
                add(vec(x + 1, y + 1))
                add(vec(x + 1, y + 1))
                add(vec(x, y + 1))
                add(vec(x, y))
            }
        }
    }
    private val uv = arrayListOf<Vector2>().apply {
        if (wireframe) {
            for (y in yFrom until yTo) {
                for (x in xFrom until xTo) {
                    add(Vector2(0f, 0f))
                    add(Vector2(1f, 0f))
                    add(Vector2(1f, 1f))
                    add(Vector2(1f, 1f))
                    add(Vector2(0f, 1f))
                    add(Vector2(0f, 0f))
                }
            }
        } else {
            for (y in 0 until yTo * 2) {
                for (x in 0 until xTo * 2) {
                    add(Vector2((x) / (xTo * 2).toFloat(), 1f - (y) / (yTo * 2).toFloat()))
                    add(Vector2((x + 1f) / (xTo * 2).toFloat(), 1f - (y) / (yTo * 2).toFloat()))
                    add(Vector2((x + 1f) / (xTo * 2).toFloat(), 1f - (y + 1f) / (yTo * 2).toFloat()))
                    add(Vector2((x + 1f) / (xTo * 2).toFloat(), 1f - (y + 1f) / (yTo * 2).toFloat()))
                    add(Vector2((x) / (xTo * 2).toFloat(), 1f - (y + 1f) / (yTo * 2).toFloat()))
                    add(Vector2((x) / (xTo * 2).toFloat(), 1f - (y) / (yTo * 2).toFloat()))
                }
            }
        }
    }

    init {
        vCount = vMaxCount
    }

    override fun array() = FloatArray(vMaxCount * vSize).also { arr ->
        for (i in 0 until vMaxCount) {
            arr[i * 5 + 0] = pos3D.x + xyz[i].x
            arr[i * 5 + 1] = pos3D.y + xyz[i].y
            arr[i * 5 + 2] = pos3D.z + xyz[i].z
            arr[i * 5 + 3] = uv[i].x
            arr[i * 5 + 4] = uv[i].y
        }
    }

    override fun onEnableVertex() {
        GLES30.glEnableVertexAttribArray(shaderProgram.vPosition)
        GLES30.glVertexAttribPointer(shaderProgram.vPosition, 3, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 0)
        GLES30.glEnableVertexAttribArray(shaderProgram.vTexturePosition)
        GLES30.glVertexAttribPointer(shaderProgram.vTexturePosition, 2, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT)
    }

    override fun onDisableVertex() {
        GLES30.glDisableVertexAttribArray(shaderProgram.vPosition)
        GLES30.glDisableVertexAttribArray(shaderProgram.vTexturePosition)
    }
}
