package pl.garedgame.game.objects

import com.google.gson.Gson
import pl.garedgame.game.GameApplication
import pl.garedgame.game.R

class Cities {

    companion object {
        val instance = load()
        private fun load(): Cities {
            val json = GameApplication.instance.resources.openRawResource(R.raw.capital_cities).
                    bufferedReader().use { it.readText() }
            return Gson().fromJson(json, Cities::class.java)
        }
    }

    val cities = arrayOf<City>()

    class City {
        val country = ""
        val capital = ""
        val lat = 0f
        val long = 0f
    }
}
