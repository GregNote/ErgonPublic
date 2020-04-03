package pl.garedgame.game.ui

import android.content.Context
import android.util.AttributeSet
import pl.garedgame.game.Game
import pl.garedgame.game.objects.Cities
import pl.garedgame.game.render.*

class MapView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null)
    : GameViewGL(context, attributeSet) {

    override val depthTest: Boolean = true
    override val camera: Camera = GlobeCamera()

    private val citiesMesh = arrayListOf<GlobeMesh>().apply {
        Cities.instance.cities.forEach {
            add(GlobeMesh(0.2f, 30, true).apply {
                setCoordinates(it.lat, it.long)
            })
        }
    }
    private val globeMesh = GlobeMesh(100f)
    private var globeTexture: Texture? = OpenGLRenderer.getTexture("globe.png")
    init {
        GameViewGL.postDelayed({
            globeTexture = OpenGLRenderer.getTexture("globe.png")
        }, 120L)
    }

    var updateCallback: (() -> Unit)? = null

    override fun update(sinceMillis: Long) {
        for (unit in Game.instance.save.units) {
            unit.update(sinceMillis)
        }
        updateCallback?.invoke()
    }

    override fun onDraw(renderer: OpenGLRenderer) {
        renderer.mvpMatrixWithCamera().also { mvpMatrix ->
            globeTexture?.bind()
            globeMesh.draw(mvpMatrix)
            Texture.Frame.bind()
            citiesMesh.forEach { it.draw(mvpMatrix) }
        }
    }
}
