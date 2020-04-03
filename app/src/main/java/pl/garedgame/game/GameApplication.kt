package pl.garedgame.game

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.SystemClock
import pl.garedgame.game.objects.Content
import pl.garedgame.game.render.TextureRegion
import pl.garedgame.game.skirmish.SceneMaps
import pl.garedgame.game.util.resizeToHeight
import pl.garedgame.game.util.resizeToWidth
import java.io.*

class GameApplication : Application() {

    companion object {
        lateinit var instance: GameApplication
    }

    private val drawables = hashMapOf<String, Drawable>()

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (!BuildConfig.DEBUG) {
            Thread.getDefaultUncaughtExceptionHandler()?.let { oldDefaultUncaughtExceptionHandler ->
                Thread.setDefaultUncaughtExceptionHandler { t, e ->
                    var report = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n${BuildConfig.GitHash}"
                    var th: Throwable? = e
                    while (th != null) {
                        val sw = StringWriter()
                        val pw = PrintWriter(sw)
                        th.printStackTrace(pw)
                        report += "\n$sw"
                        th = th.cause
                    }

                    val intent = Intent(this, CrashReceiver::class.java)
                    intent.putExtra("callstack", report)
                    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                    val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 300L, pendingIntent)

                    oldDefaultUncaughtExceptionHandler.uncaughtException(t, e)
                }
            }
        }
        TextureRegion.loadRegions()
        Game.content = Content.loadContent(this)
        SceneMaps.loadContent(this)
        Configuration.loadConfigurations()
        SoundPlayer.instance.loadSounds()
    }
    private fun getAssetsDrawable(name: String): Drawable? {
        runCatching {
            return Drawable.createFromStream(assets.open(name), null)
        }
        var drawable: Drawable? = null
        runOnAssets({ imgPath ->
            runCatching {
                if (drawable == null && imgPath.endsWith(name)) {
                    drawable = Drawable.createFromStream(assets.open(imgPath), null)
                }
            }
        }, "imgs")
        return drawable
    }

    fun getAssetsDrawableByWidth(name: String, width: Int): Drawable? {
        val key = "${name}_W_${width}"
        if (drawables.contains(key)) { return drawables[key] }
        val drawable = getAssetsDrawable(name)?.let { resizeToWidth(it, width) }
        drawable?.also { drawables[key] = it }
        return drawable
    }

    fun getAssetsDrawableByHeight(name: String, height: Int): Drawable? {
        val key = "${name}_H_${height}"
        if (drawables.contains(key)) { return drawables[key] }
        val drawable = getAssetsDrawable(name)?.let { resizeToHeight(it, height) }
        drawable?.also { drawables[key] = it }
        return drawable
    }

    fun runOnAssets(callback: (String) -> Unit, path: String = "") {// if is directory
        assets.list(path)?.let { listed ->
            if (listed.isEmpty()) {
                callback(path)
            } else {
                for (i in listed.indices) {
                    runOnAssets(callback, "${path}/${listed[i]}")
                }
            }
        }
    }

    fun writeToFile(data: String, file: String) {
        try {
            OutputStreamWriter(openFileOutput(file, Context.MODE_PRIVATE)).apply {
                write(data)
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readFromFile(file: String): String {
        try {
            openFileInput(file)?.also {
                InputStreamReader(it).apply {
                    val strBuilder = StringBuilder()
                    BufferedReader(this).apply {
                        var str = ""
                        while ( readLine()?.let { str = it } != null ) {
                            strBuilder.append(str)
                        }
                    }
                    close()
                    return strBuilder.toString()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}
