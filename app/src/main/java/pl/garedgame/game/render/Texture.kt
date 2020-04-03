package pl.garedgame.game.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import pl.garedgame.game.GameApplication
import java.io.IOException
import java.io.InputStream
import java.nio.IntBuffer

open class Texture(var path: String) {

    companion object {
        val Empty = DynamicTexture(2, 2).apply {
            set(0, 0, 0xffffffff.toInt()); set(1, 0, 0x000000ff)
            set(0, 1, 0x000000ff); set(1, 1, 0xffffffff.toInt())
        }
        val Transparent = DynamicTexture(2, 2).apply {
            clear(0x00000000)
        }
        val Frame = DynamicTexture(4, 4).apply {
            clear(0x994444ff.toInt())
        }
        var currentTextureId = -1

        private fun createTexture(width: Int, height: Int, pixelsBuffer: IntBuffer?, textureFilter: Int): Int {
            val textureID = IntArray(1).let {
                GLES30.glGenTextures(1, it, 0)
                it[0]
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID)
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D, 0,
                    GLES30.GL_RGBA, width, height, 0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE, pixelsBuffer
            )
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, textureFilter)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, textureFilter)
            return textureID
        }

        fun createFBOTexture(width: Int, height: Int, textureFilter: Int = GLES30.GL_NEAREST, pixelsBuffer: IntBuffer? = null): Int {
            val handleID = IntArray(1).let {
                GLES30.glGenFramebuffers(1, it, 0)
                it[0]
            }

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, handleID)
            val fboTex = createTexture(width, height, pixelsBuffer, textureFilter)
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, fboTex, 0)
            check(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) == GLES30.GL_FRAMEBUFFER_COMPLETE) { "GL_FRAMEBUFFER status incomplete" }

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
            return handleID
        }
    }

    var textureId: Int = -1

    protected open fun load() {
        val bitmap = loadBitmapFromAssets()
        textureId = createFBOTexture(bitmap.width, bitmap.height)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, bitmap)
    }

    open fun bind() {
        if (textureId == -1 && path.isNotEmpty()) load()
        if (textureId != currentTextureId) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            currentTextureId = textureId
        }
    }

    private fun loadBitmapFromAssets(): Bitmap {
        var stream: InputStream? = null
        try {
            stream = GameApplication.instance.assets.open(path)
            return BitmapFactory.decodeStream(stream)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } finally {
            stream?.let { try { it.close() } catch (ignored: IOException) { } }
        }
    }
}