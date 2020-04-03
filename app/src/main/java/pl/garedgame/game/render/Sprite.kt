package pl.garedgame.game.render

import android.opengl.GLES30
import pl.garedgame.game.util.BYTES_PER_FLOAT

class ColorSprite(r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f) : Mesh() {
    private val colors = FloatArray(16)
    override val shaderProgram: ShaderProgram = ColorShader.instance
    override val vSize: Int = 6
    override val vMaxCount: Int = 6
    override fun array() = floatArrayOf(
            vecX(-0.5f, -0.5f), vecY(-0.5f, -0.5f), colors[0], colors[1], colors[2], colors[3],//top left
            vecX(+0.5f, -0.5f), vecY(+0.5f, -0.5f), colors[4], colors[5], colors[6], colors[7],//top right
            vecX(+0.5f, +0.5f), vecY(+0.5f, +0.5f), colors[8], colors[9], colors[10], colors[11],//bottom right
            vecX(+0.5f, +0.5f), vecY(+0.5f, +0.5f), colors[8], colors[9], colors[10], colors[11],//bottom right
            vecX(-0.5f, +0.5f), vecY(-0.5f, +0.5f), colors[12], colors[13], colors[14], colors[15],//bottom left
            vecX(-0.5f, -0.5f), vecY(-0.5f, -0.5f), colors[0], colors[1], colors[2], colors[3]//top left
    )
    init {
        vCount = 6
        changeColor(r, g, b, a)
    }
    fun changeColor(r: Float, g: Float, b: Float, a: Float = 1f) {
        colors[0] = r;colors[1] = g;colors[2] = b;colors[3] = a
        colors[4] = r;colors[5] = g;colors[6] = b;colors[7] = a
        colors[8] = r;colors[9] = g;colors[10] = b;colors[11] = a
        colors[12] = r;colors[13] = g;colors[14] = b;colors[15] = a
    }
    override fun onEnableVertex() {
        super.onEnableVertex()
        GLES30.glEnableVertexAttribArray(shaderProgram.vColor)
        GLES30.glVertexAttribPointer(shaderProgram.vColor, 4, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 2 * BYTES_PER_FLOAT)
    }
    override fun onDisableVertex() {
        super.onDisableVertex()
        GLES30.glDisableVertexAttribArray(shaderProgram.vColor)
    }
}

class TextureSprite(
        var textureRegion: TextureRegion = TextureRegion.EMPTY
) : Mesh() {
    override val shaderProgram: ShaderProgram = TextureShader.instance
    override val vSize: Int = 4
    override val vMaxCount: Int = 6
    override fun array() = floatArrayOf(
            vecX(-0.5f, -0.5f), vecY(-0.5f, -0.5f), textureRegion.x + 0.005f, textureRegion.yh - 0.005f,//top left
            vecX(+0.5f, -0.5f), vecY(+0.5f, -0.5f), textureRegion.xw - 0.005f, textureRegion.yh - 0.005f,//top right
            vecX(+0.5f, +0.5f), vecY(+0.5f, +0.5f), textureRegion.xw - 0.005f, textureRegion.y + 0.005f,//bottom right
            vecX(+0.5f, +0.5f), vecY(+0.5f, +0.5f), textureRegion.xw - 0.005f, textureRegion.y + 0.005f,//bottom right
            vecX(-0.5f, +0.5f), vecY(-0.5f, +0.5f), textureRegion.x + 0.005f, textureRegion.y + 0.005f,//bottom left
            vecX(-0.5f, -0.5f), vecY(-0.5f, -0.5f), textureRegion.x + 0.005f, textureRegion.yh - 0.005f//top left
        )
    init {
        vCount = 6
    }
    override fun onEnableVertex() {
        super.onEnableVertex()
        GLES30.glEnableVertexAttribArray(shaderProgram.vTexturePosition)
        GLES30.glVertexAttribPointer(shaderProgram.vTexturePosition, 2, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 2 * BYTES_PER_FLOAT)
    }
    override fun onDisableVertex() {
        super.onDisableVertex()
        GLES30.glDisableVertexAttribArray(shaderProgram.vTexturePosition)
    }
}

class ColorSpriteBatch(override val static: Boolean = false, override val vMaxCount: Int = 4096 * 6) : Mesh() {
    override val shaderProgram: ShaderProgram = ColorShader.instance
    override val vSize: Int = 6
    private val sprites = arrayListOf<ColorSprite>()

    fun clearSprites() {
        sprites.clear()
        needUpdate = true
    }

    fun add(sprite: ColorSprite) = sprites.add(sprite).also {
        if (it) needUpdate = true
    }
    fun remove(sprite: ColorSprite) = sprites.remove(sprite).also {
        if (it) needUpdate = true
    }

    override fun updateBuffer() {
        vCount = 0
        buffer.position(0)
        sprites.forEach {
            val array = it.array()
            buffer.put(array)
            vCount += it.vCount
        }
        buffer.position(0)
    }
    override fun onEnableVertex() {
        super.onEnableVertex()
        GLES30.glEnableVertexAttribArray(shaderProgram.vColor)
        GLES30.glVertexAttribPointer(shaderProgram.vColor, 4, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 2 * BYTES_PER_FLOAT)
    }
    override fun onDisableVertex() {
        super.onDisableVertex()
        GLES30.glDisableVertexAttribArray(shaderProgram.vColor)
    }
}

class TextureSpriteBatch(override val static: Boolean = false, override val vMaxCount: Int = 4096 * 6) : Mesh() {
    override val shaderProgram: ShaderProgram = TextureShader.instance
    override val vSize: Int = 4
    private val sprites = arrayListOf<TextureSprite>()

    fun clearSprites() {
        sprites.clear()
        needUpdate = true
    }

    fun add(sprite: TextureSprite) = sprites.add(sprite).also {
        if (it) needUpdate = true
    }
    fun remove(sprite: TextureSprite) = sprites.remove(sprite).also {
        if (it) needUpdate = true
    }

    override fun updateBuffer() {
        vCount = 0
        buffer.position(0)
        sprites.forEach {
            val array = it.array()
            buffer.put(array)
            vCount += it.vCount
        }
        buffer.position(0)
    }
    override fun onEnableVertex() {
        super.onEnableVertex()
        GLES30.glEnableVertexAttribArray(shaderProgram.vTexturePosition)
        GLES30.glVertexAttribPointer(shaderProgram.vTexturePosition, 2, GLES30.GL_FLOAT, false, vSize * BYTES_PER_FLOAT, 2 * BYTES_PER_FLOAT)
    }
    override fun onDisableVertex() {
        super.onDisableVertex()
        GLES30.glDisableVertexAttribArray(shaderProgram.vTexturePosition)
    }
}
