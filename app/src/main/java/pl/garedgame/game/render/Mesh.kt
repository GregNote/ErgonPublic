package pl.garedgame.game.render

import android.opengl.GLES30
import pl.garedgame.game.SpaceOrientation2D
import pl.garedgame.game.Vector2
import pl.garedgame.game.collisions.Polygon
import pl.garedgame.game.entities.WithPolygon
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.util.BYTES_PER_FLOAT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

abstract class Mesh {
    companion object {
        private fun genVertexArray(): Int {
            return intArrayOf(0).apply { GLES30.glGenVertexArrays(1, this, 0) }[0]
        }
        private fun genBuffer(): Int {
            return intArrayOf(0).apply { GLES30.glGenBuffers(1, this, 0) }[0]
        }
    }

    abstract val shaderProgram: ShaderProgram
    lateinit var buffer: FloatBuffer
    abstract val vSize: Int
    abstract val vMaxCount: Int
    private var initialized = false

    var orientation = SpaceOrientation2D()

    fun vecX(x: Float, y: Float): Float {
        val rad = Math.toRadians(orientation.rotation.toDouble())
        return orientation.pos.x + cos(rad).toFloat() * (x * orientation.scaleX) - sin(rad).toFloat() * (y * orientation.scaleY)
    }

    fun vecY(x: Float, y: Float): Float {
        val rad = Math.toRadians(orientation.rotation.toDouble())
        return orientation.pos.y + sin(rad).toFloat() * (x * orientation.scaleX) + cos(rad).toFloat() * (y * orientation.scaleY)
    }

    var vCount: Int = 0
        protected set

    protected open val static: Boolean = false
    protected var needUpdate = false

    private var vao: Int = 0
    private var vbo: Int = 0

    protected open fun init() {
        buffer = ByteBuffer.allocateDirect(vMaxCount * vSize * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    fun setNeedUpdate() {
        needUpdate = true
    }

    open fun array(): FloatArray = floatArrayOf()
    open fun updateBuffer() {
        buffer.position(0)
        buffer.put(array())
    }

    protected open fun onBindBuffer() {
        buffer.position(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vCount * vSize * BYTES_PER_FLOAT, buffer, GLES30.GL_DYNAMIC_DRAW)
    }
    protected open fun onEnableVertex() {
        GLES30.glEnableVertexAttribArray(shaderProgram.vPosition)
        GLES30.glVertexAttribPointer(shaderProgram.vPosition, 2, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 0)
    }
    protected open fun onDisableVertex() {
        GLES30.glDisableVertexAttribArray(shaderProgram.vPosition)
    }
    protected open fun onDraw() {
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount)
    }
    fun draw(mvpMatrix: FloatArray) {
        shaderProgram.use()
        if (!initialized) {
            vbo = genBuffer()
            if (static) vao = genVertexArray()
            init()
            initialized = true
        }
        GLES30.glUniformMatrix4fv(shaderProgram.uMVPMatrix, 1, false, mvpMatrix, 0)

        if (static) {
            GLES30.glBindVertexArray(vao)
            if (needUpdate) {
                updateBuffer()
                onBindBuffer()
                onEnableVertex()
            }
            onDraw()
            GLES30.glBindVertexArray(0)
            if (needUpdate) {
                onDisableVertex()
                needUpdate = false
            }
        } else {
            updateBuffer()
            onBindBuffer()
            onEnableVertex()
            onDraw()
            onDisableVertex()
        }
    }
}

class ViewMesh : Mesh() {
    private val resolution = 20
    override val shaderProgram: ShaderProgram = TextureShader.instance
    override val vSize: Int = 4
    override val vMaxCount: Int = 16 * resolution * 3
    private val units = arrayListOf<GameUnit>()
    private val hitInfos = Array(resolution + 1) { HitInfo(null, 0f, Vector2.ZERO) }
    private val pomHitInfo = HitInfo(null, 0f, Vector2.ZERO)
    private val pomVec = Vector2.ZERO

    fun clearUnits() {
        units.clear()
        needUpdate = true
    }

    fun add(unit: GameUnit) = units.add(unit).also {
        if (it) needUpdate = true
    }

    private fun checkHit(unit: GameUnit, rangeOfViewY: Float, hitInfo: HitInfo) {
        unit.gameScene?.also { scene ->
            val rad = Math.toRadians(hitInfo.rotation.toDouble())
            hitInfo.pos.x = unit.orientation.pos.x - sin(rad).toFloat() * rangeOfViewY
            hitInfo.pos.y = unit.orientation.pos.y + cos(rad).toFloat() * rangeOfViewY
            hitInfo.polygon = scene.rayCast(unit.orientation.pos, hitInfo.pos) { polygon ->
                polygon.gameObject !is GameUnit &&
                        polygon.gameObject?.let {
                            it is WithPolygon && it.collide
                        } ?: false
            }?.also {
                unit.orientation.pos.vectorTo(it.second, pomVec)
                pomVec.normalize()
                pomVec.timesAssign(0.1f)
                hitInfo.pos.set(it.second)
                hitInfo.pos.plusAssign(pomVec)
            }?.first
        }
    }

    private fun findEdge(unit: GameUnit, rangeOfViewY: Float, minHitInfo: HitInfo, maxHitInfo: HitInfo) {
        for (i in 1..4) {
            pomHitInfo.rotation = (minHitInfo.rotation + maxHitInfo.rotation) / 2f
            checkHit(unit, rangeOfViewY, pomHitInfo)
            if (pomHitInfo.polygon == minHitInfo.polygon) {
                minHitInfo.pos.set(pomHitInfo.pos)
                minHitInfo.rotation = pomHitInfo.rotation
            }
            if (pomHitInfo.polygon == maxHitInfo.polygon) {
                maxHitInfo.pos.set(pomHitInfo.pos)
                maxHitInfo.rotation = pomHitInfo.rotation
            }
        }
    }

    override fun updateBuffer() {
        vCount = 0
        buffer.position(0)
        units.forEach { unit ->
            val rotationStep = unit.getFieldOfView() / resolution.toFloat()
            var rotation = unit.orientation.rotation - unit.getFieldOfView() * 0.5f
            val rangeOfViewY = -unit.getRangeOfView()

            for (i in hitInfos.indices) {
                hitInfos[i].rotation = rotation
                checkHit(unit, rangeOfViewY, hitInfos[i])
                rotation += rotationStep
                if (i > 0 && hitInfos[i].polygon != hitInfos[i - 1].polygon) {
                    findEdge(unit, rangeOfViewY, hitInfos[i], hitInfos[i - 1])
                }
            }
            for (i in 0 until resolution) {
                buffer.put(unit.orientation.pos.x).put(unit.orientation.pos.y).put(0.5f).put(0.5f)
                buffer.put(hitInfos[i].pos.x).put(hitInfos[i].pos.y).put(0.5f).put(0.5f)
                buffer.put(hitInfos[i + 1].pos.x).put(hitInfos[i + 1].pos.y).put(0.5f).put(0.5f)
                vCount += 3
            }
        }
        buffer.position(0)
    }

    class HitInfo(var polygon: Polygon?, var rotation: Float, val pos: Vector2)
}
