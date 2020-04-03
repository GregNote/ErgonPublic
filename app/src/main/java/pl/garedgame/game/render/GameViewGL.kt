package pl.garedgame.game.render

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import pl.garedgame.game.Game
import pl.garedgame.game.GameApplication
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import kotlin.math.pow
import kotlin.math.sqrt

@Suppress("UNUSED_PARAMETER")
abstract class GameViewGL @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : GLSurfaceView(GameApplication.instance, attrs), OpenGLRenderer.Listener {

    companion object {
        private val actions = ArrayDeque<Post>()
        fun post(action: () -> Unit) {
            post(Runnable { run(action) })
        }
        fun post(runnable: Runnable) {
            actions.push(Post(runnable))
        }
        fun postDelayed(action: () -> Unit, delay: Long) {
            postDelayed(Runnable { run(action) }, delay)
        }
        fun postDelayed(runnable: Runnable, delay: Long) {
            actions.push(Post(runnable, System.currentTimeMillis() + delay))
        }
    }

    abstract val depthTest: Boolean
    abstract val camera: Camera

    private var initialized = false
    protected var redSize = 8
    protected var greenSize = 8
    protected var blueSize = 8
    protected var alphaSize = 8
    protected var depthSize = 16
    protected var sampleSize = 4
    protected var stencilSize = 8
    protected var value = IntArray(1)

    private var pressPosX = 0f
    private var pressPosY = 0f
    private var lastPosX = 0f
    private var lastPosY = 0f
    private var prevCameraZ = 0f
    private var zoomDist = 0f

    private fun initialize() {
        if (!initialized) {
            setEGLContextFactory(ContextFactory())
            setEGLConfigChooser(ConfigChooser())
            preserveEGLContextOnPause = true
            setRenderer(OpenGLRenderer(this, depthTest))
            prevCameraZ = camera.getZoom()
            initialized = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initialize()
        Game.instance.save.timer.onUpdate = { update(it) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Game.instance.save.timer.onUpdate = null
    }

    override fun updateProjectionMatrix(width: Int, height: Int) {
        camera.updateProjectionMatrix(width, height)
    }

    override fun updateMatrix(mvpMatrix: FloatArray) {
        camera.updateMatrix(mvpMatrix)
    }

    override fun onResume() {
        initialize()
        super.onResume()
    }

    override fun onUpdate(since: Long) {
        if (actions.isNotEmpty()) {
            val action = actions.pop()
            if (action.delay > System.currentTimeMillis()) {
                actions.push(action)
            } else {
                action.runnable.run()
            }
        }
        Game.instance.save.timer.onUpdate(since)
    }

    abstract fun update(sinceMillis: Long)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                when (motionEvent.pointerCount) {
                    1 -> {
                        pressPosX = motionEvent.x
                        pressPosY = motionEvent.y
                        lastPosX = pressPosX
                        lastPosY = pressPosY
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (motionEvent.pointerCount == 1 && pressPosX >= 0f && pressPosY >= 0f) {
                    val dist = sqrt(((pressPosX - motionEvent.x).pow(2) + (pressPosY - motionEvent.y).pow(2)).toDouble()).toFloat()
                    if (dist <= 10f) {
                        camera.click(motionEvent.x, motionEvent.y)
                    }
                    pressPosX = -1f
                    pressPosY = -1f
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (motionEvent.pointerCount) {
                    1 -> {
                        camera.moveCamera(motionEvent.x - lastPosX, motionEvent.y - lastPosY)
                        lastPosX = motionEvent.x
                        lastPosY = motionEvent.y
                    }
                    2 -> {
                        val (x1: Float, y1: Float) = motionEvent.findPointerIndex(motionEvent.getPointerId(0)).let {
                            motionEvent.getX(it) to motionEvent.getY(it)
                        }
                        val (x2: Float, y2: Float) = motionEvent.findPointerIndex(motionEvent.getPointerId(1)).let {
                            motionEvent.getX(it) to motionEvent.getY(it)
                        }
                        val midX = (x1 + x2) / 2f
                        val midY = (y1 + y2) / 2f
                        val newZoomDist = sqrt(((x1 - x2).pow(2) + (y1 - y2).pow(2)).toDouble()).toFloat()
                        camera.setZoom(prevCameraZ / (newZoomDist / zoomDist))
                        camera.moveCamera(midX - lastPosX, midY - lastPosY)
                        lastPosX = midX
                        lastPosY = midY
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (motionEvent.pointerCount == 2) {
                    val (x1: Float, y1: Float) = motionEvent.findPointerIndex(motionEvent.getPointerId(0)).let {
                        motionEvent.getX(it) to motionEvent.getY(it)
                    }
                    val (x2: Float, y2: Float) = motionEvent.findPointerIndex(motionEvent.getPointerId(1)).let {
                        motionEvent.getX(it) to motionEvent.getY(it)
                    }
                    lastPosX = (x1 + x2) / 2f
                    lastPosY = (y1 + y2) / 2f
                    prevCameraZ = camera.getZoom()
                    zoomDist = sqrt(((x1 - x2).pow(2) + (y1 - y2).pow(2)).toDouble()).toFloat()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (motionEvent.pointerCount == 2) {
                    val (x1: Float, y1: Float) = when ((motionEvent.action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT) {
                        0 -> motionEvent.findPointerIndex(motionEvent.getPointerId(1)).let {
                            motionEvent.getX(it) to motionEvent.getY(it)
                        }
                        1 -> motionEvent.findPointerIndex(motionEvent.getPointerId(0)).let {
                            motionEvent.getX(it) to motionEvent.getY(it)
                        }
                        else -> 0f to 0f
                    }
                    lastPosX = x1
                    lastPosY = y1
                }
            }
        }
        return true
    }

    class Post(val runnable: Runnable, val delay: Long = 0L)

    private inner class ContextFactory : EGLContextFactory {

        override fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
            val eglContextClientVersion = 0x3098
            val attributesList = intArrayOf(eglContextClientVersion, 3, EGL10.EGL_NONE)
            return egl!!.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attributesList)
        }

        override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
            egl!!.eglDestroyContext(display, context)
        }
    }

    private inner class ConfigChooser : EGLConfigChooser {

        override fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig {
            val eglOpenGLEs2Bit = 4
            val configAttributes = intArrayOf(
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_RENDERABLE_TYPE, eglOpenGLEs2Bit,
                    EGL10.EGL_SAMPLES, sampleSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE
            )
            val numConfig = IntArray(1)
            egl!!.eglChooseConfig(display, configAttributes, null, 0, numConfig)
            val configs = Array<EGLConfig?>(numConfig[0]) { null }
            egl.eglChooseConfig(display, configAttributes, configs, numConfig[0], numConfig)
            return selectConfig(egl, display, configs)!!
        }

        fun selectConfig(egl: EGL10, display: EGLDisplay?, configs: Array<EGLConfig?>): EGLConfig? {
            for(config in configs) {
                val d = getConfigAttributes(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
                val s = getConfigAttributes(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0)
                val r = getConfigAttributes(egl, display, config, EGL10.EGL_RED_SIZE,0)
                val g = getConfigAttributes(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                val b = getConfigAttributes(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
                val a = getConfigAttributes(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
                if (r == redSize && g == greenSize && b == blueSize && a == alphaSize && d >= depthSize && s >= stencilSize)
                    return config
            }
            return null
        }

        fun getConfigAttributes(
                egl: EGL10,
                display: EGLDisplay?,
                config: EGLConfig?,
                attribute: Int,
                defaultValue: Int
        ): Int {
            return if (egl.eglGetConfigAttrib(display, config, attribute, value)) value[0] else defaultValue
        }
    }
}
