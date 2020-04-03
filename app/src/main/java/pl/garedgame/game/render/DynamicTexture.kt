package pl.garedgame.game.render

import android.graphics.Color
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class DynamicTexture(
        val w: Int,
        val h: Int
) : Texture("DT") {

    val w2 = w / 2
    val h2 = h / 2
    private var needUpdate = false
    private val pixels = IntArray(w * h) { Color.TRANSPARENT }

    fun clear(color: Int) = pixels.fill(color)

    operator fun set(x: Int, y: Int, pixel: Int) {
        if (x in 0 until w && y in 0 until h) {
            needUpdate = pixels[y * w + x] != pixel
            pixels[y * w + x] = pixel
        }
    }

    fun get(x: Int, y: Int): Int {
        return if (x in 0 until w && y in 0 until h) pixels[y * w + x] else 0
    }

    fun perPixel(callback: (Int) -> Int) {
        for (i in pixels.indices) pixels[i] = callback(pixels[i])
    }

    private fun pixelsBuffer(): IntBuffer {
        return ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder())
                .asIntBuffer().apply {
                    put(pixels)
                    position(0)
                }
    }

    fun update() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, w, h,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixelsBuffer())
        needUpdate = false
    }

    override fun load() {
        textureId = createFBOTexture(w, h, GLES30.GL_LINEAR, pixelsBuffer())
        update()
    }

    override fun bind() {
        super.bind()
        if (needUpdate) update()
    }
}
