package pl.garedgame.game.util

import android.content.Context
import android.content.SharedPreferences
import pl.garedgame.game.GameApplication

class SharedPrefs {
    companion object {
        private fun getPrefs(name: String): SharedPreferences {
            val context = GameApplication.instance
            return context.getSharedPreferences(
                    "${context.packageName}.$name",
                    Context.MODE_PRIVATE
            )
        }

        fun putString(name: String, key: String, value: String) {
            getPrefs(name).edit().putString(key, value).apply()
        }

        fun getString(name: String, key: String): String {
            return getPrefs(name).getString(key, "") ?: ""
        }

        fun doOnPrefs(name: String, callback: (SharedPreferences) -> Unit) {
            callback(getPrefs(name))
        }

    }
}
