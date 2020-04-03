package pl.garedgame.game.ui

import android.content.Context
import android.util.AttributeSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.garedgame.game.Game
import pl.garedgame.game.entities.GameScene
import pl.garedgame.game.fragments.EditorFragment
import pl.garedgame.game.render.Camera
import pl.garedgame.game.render.GameViewGL
import pl.garedgame.game.render.OpenGLRenderer
import pl.garedgame.game.render.SkirmishCamera
import pl.garedgame.game.skirmish.SceneMaps

class GameSceneView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : GameViewGL(context, attrs) {

    override val depthTest: Boolean = false
    override val camera: Camera = SkirmishCamera()

    var scene = GameScene(64f, 64f, 0.25f)
    override fun onDraw(renderer: OpenGLRenderer) = scene.draw(renderer)
    override fun update(sinceMillis: Long) = scene.update(sinceMillis)

    fun loadScene(index: Int, sceneLoaded: () -> Unit) {
        GameViewGL.post {
            scene = GameScene(SceneMaps.instance.maps[index])
            sceneLoaded()
        }
    }

    fun loadEditorScene(sceneLoaded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val sceneMap = Game.instance.loadSceneMap()
            GameViewGL.post {
                scene = EditorFragment.EditorGameScene(sceneMap)
                sceneLoaded()
            }
        }
    }
}
