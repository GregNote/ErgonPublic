package pl.garedgame.game.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_crash.*
import pl.garedgame.game.Game
import pl.garedgame.game.R

class CrashActivity : AppCompatActivity() {
    private var callstack = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        if (intent.hasExtra("callstack")) {
            callstack = intent.getStringExtra("callstack") ?: ""
        }
        exception.text = callstack
        copyReport.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("fullReport", getFullReport()))
            Toast.makeText(this, "Raport skopiowany do schowka", Toast.LENGTH_SHORT).show()
        }
        addSaveToReport.visibility = if (Game.instance.hasSavedGame()) View.VISIBLE else View.GONE
        sendReport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse(
                    "mailto:gared1991+crash@gmail.com" +
                            "?subject=Raport" +
                            "&body=${getFullReport()}"
            )
            startActivity(intent)
        }
    }

    private fun getFullReport(): String {
        return if (Game.instance.hasSavedGame() && addSaveToReport.isChecked) {
            callstack + Game.instance.loadGameJsonString()
        } else callstack
    }
}
