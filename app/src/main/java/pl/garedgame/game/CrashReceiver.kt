package pl.garedgame.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pl.garedgame.game.activities.CrashActivity

class CrashReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val newIntent = Intent(context, CrashActivity::class.java)
        if (intent.hasExtra("callstack")) {
            newIntent.putExtra("callstack", intent.getStringExtra("callstack"))
        }
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(newIntent)
    }
}
