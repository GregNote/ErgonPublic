package pl.garedgame.game

import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import pl.garedgame.game.tooltip.TooltipPopup
import pl.garedgame.game.util.SharedPrefs

class Tutorial private constructor() {

    companion object {
        private var tutPopup: TutorialTooltip? = null

        val instance = SharedPrefs.getString("TUTORIAL", "TUTORIAL").let {
            if (it.isNotEmpty()) {
                Gson().fromJson(it, Tutorial::class.java)
            } else Tutorial()
        }

        private fun save() {
            SharedPrefs.putString("TUTORIAL", "TUTORIAL", Gson().toJson(instance))
        }
    }

    private val doneTuts = arrayListOf<Tut>()

    fun canShowTut(tut: Tut) = !doneTuts.contains(tut)

    fun tryShowTut(tut: Tut, anchor: View, gravity: Int) {
        if (canShowTut(tut)) showTut(tut, anchor, gravity)
    }

    fun showTut(tut: Tut, anchor: View, gravity: Int) {
        tutPopup = TutorialTooltip(tut).apply { showAt(anchor, gravity) }
    }

    fun tutDone(tut: Tut) {
        if (!doneTuts.contains(tut)) {
            doneTuts.add(tut)
            save()
        }
    }

    enum class Tut {
        MainFirstStart,
        SkirmishManageUnits,
        SkirmishAddOponent,
        SkirmishStartNew
    }

    class TutorialTooltip(tut: Tut) : TooltipPopup(R.layout.popup_tutorial) {
        init {
            contentView.findViewById<TextView>(R.id.tutorialInfo).text = tut.name
            onDismiss = {
                instance.tutDone(tut)
            }
        }
    }
}
