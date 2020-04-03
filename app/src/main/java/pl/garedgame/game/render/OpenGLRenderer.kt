package pl.garedgame.game.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import pl.garedgame.game.GameApplication
import pl.garedgame.game.util.doOnMainThread
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val listener: Listener, private val depth: Boolean = false) : GLSurfaceView.Renderer {

    interface Listener {
        fun updateProjectionMatrix(width: Int, height: Int)
        fun updateMatrix(mvpMatrix: FloatArray)
        fun onUpdate(since: Long)
        fun onDraw(renderer: OpenGLRenderer)
    }

    companion object {
        private var fpsListener: ((Int) -> Unit)? = null
        private val textures = hashMapOf<String, Texture>()

        fun setFpsListener(listener: ((Int) -> Unit)?) { fpsListener = listener }

        private fun prepareTexture(path: String) {
            if (!textures.containsKey(path)) {
                textures[path] = Texture(path)
            }
        }

        fun getTexture(path: String): Texture {
            val slashIndex = path.indexOf('/')
            val image = if (slashIndex != -1) {
                path.substring(slashIndex + 1)
            } else path
            textures.keys.find { it.endsWith("/${image}") }?.also {
                return textures[it] ?: Texture.Empty
            }
            return Texture.Empty
        }

        fun getAllTextureNames(): List<String> {
            return textures.keys.map { it.substring(it.lastIndexOf('/') + 1) }
        }
    }

    private var time = System.currentTimeMillis()
    private var lastTime = System.currentTimeMillis()
    private var fps = 0

    private val mvpMatrix = FloatArray(16)
    private val mvpMatrixCamera = FloatArray(16)

    private var timestamp = System.currentTimeMillis()
    private fun getSinceMillis(): Long {
        val current = System.currentTimeMillis()
        val result = current - timestamp
        timestamp = current
        return  result
    }

    fun mvpMatrixWithCamera(): FloatArray {
        mvpMatrixCamera.copyInto(mvpMatrix)
        return mvpMatrix
    }

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        ColorShader.instance.load()
        GlobeShader.instance.load()
        TextureShader.instance.load()
        GameApplication.instance.runOnAssets({ prepareTexture(it) }, "imgs")
        Texture.Empty.textureId = -1
        Texture.Transparent.textureId = -1
        Texture.Frame.textureId = -1
        textures.forEach { it.value.textureId = -1 }
        GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_BLEND)
        if (depth) {
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_CULL_FACE)
        }
        GLES30.glEnable(GLES30.GL_STENCIL)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)
    }

    fun drawWithMask(
            drawMask: () -> Unit,
            drawEqualToMask: () -> Unit,
            drawNotEqualToMask: () -> Unit
    ) {
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xff)
        GLES30.glStencilOp(GLES30.GL_REPLACE, GLES30.GL_REPLACE, GLES30.GL_REPLACE)
        drawMask()
        GLES30.glStencilFunc(GLES30.GL_EQUAL, 1, 0xff)
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_KEEP)
        drawEqualToMask()
        GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xff)
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_KEEP)
        drawNotEqualToMask()
        GLES30.glDisable(GLES30.GL_STENCIL_TEST)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        listener.updateProjectionMatrix(width, height)
    }

    private fun fpsCheck() {
        ++fps
        time = System.currentTimeMillis()
        if (lastTime + 1000L < time) {
            lastTime = time
            val a = fps
            doOnMainThread { fpsListener?.invoke(a) }
            fps = 0
        }
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        listener.onUpdate(getSinceMillis())
        listener.updateMatrix(mvpMatrixCamera)
        listener.onDraw(this)
        fpsCheck()
    }
}
