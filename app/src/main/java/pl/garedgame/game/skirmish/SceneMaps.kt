package pl.garedgame.game.skirmish

import android.content.Context
import com.google.gson.annotations.Expose
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.entities.GameObject
import pl.garedgame.game.entities.GameScene

class SceneMap(
        @Expose val width: Float = 64f,
        @Expose val height: Float = 64f,
        @Expose val nodeRadius: Float = 0.25f,
        @Expose val objects: Array<GameObject> = arrayOf()
) {
    constructor(gameScene: GameScene) : this(gameScene.width, gameScene.height, gameScene.nodeRadius, gameScene.getObjectsArray())
}

class SceneMaps(@Expose val maps: Array<SceneMap>) {
    companion object {
        lateinit var instance: SceneMaps
        fun loadContent(ctx: Context) {
            val json = ctx.resources.openRawResource(R.raw.scenes).bufferedReader().use { it.readText() }
            instance = Game.instance.gson.fromJson(json, SceneMaps::class.java)
        }
    }
}
