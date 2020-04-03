package pl.garedgame.game.render

import com.google.gson.Gson
import pl.garedgame.game.GameApplication
import pl.garedgame.game.R

class TextureRegion(
        val texturePath: String,
        val name: String = "",
        val x: Float = 0f,
        val y: Float = 0f,
        val w: Float = 1f,
        val h: Float = 1f
) {
    val texture: Texture
        get() {
            return OpenGLRenderer.getTexture(texturePath)
        }

    val xw: Float
        get() = x + w
    val yh: Float
        get() = y + h

    companion object {
        val EMPTY = TextureRegion("")
        private val regions = HashMap<String, TextureRegion>()
        fun loadRegions() {
            regions.clear()
            val json = GameApplication.instance.resources.openRawResource(R.raw.texture_regions).
                    bufferedReader().use { it.readText() }
            val textureRegions = Gson().fromJson(json, TextureRegions::class.java)
            for (region in textureRegions.regions) regions[region.name] = region
        }
        fun getRegion(key: String): TextureRegion? = regions[key]
        fun getAllRegions() = regions.values
        fun getAllRegionNames() = regions.keys
    }

    class TextureRegions(val regions: ArrayList<TextureRegion>)
}
