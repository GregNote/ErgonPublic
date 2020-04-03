package pl.garedgame.game.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.ui.BaseView

class ChooseBuildingDialog(context: Context, listener: ChooseBuildingDialogListener) : Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_choose_building)
        val costs = findViewById<TextView>(R.id.costs)
        var chosenBuilding = -1
        val choose = findViewById<TextView>(R.id.choose).apply {
            setBounceOnClick {
                listener.onChooseBuilding(Game.content.buildings[chosenBuilding].key)
                setOnDismissListener(null)
                dismiss()
            }
        }
        findViewById<BaseView>(R.id.buildingsView).apply {
            getBuilding = {
                if (it < Game.content.buildings.size) {
                    Game.content.buildings[it]
                } else {
                    null
                }
            }
            onClick = {
                if (it < Game.content.buildings.size) {
                    chosenBuilding = it
                    val building = Game.content.buildings[it]
                    var description = building.name
                    building.cost.time?.apply { description += "\n■ $this time" }
                    building.cost.prefabricats?.apply { description += "\n■ $this prefabricats" }
                    building.cost.platinum?.apply { description += "\n■ $this platinum" }
                    building.cost.titanium?.apply { description += "\n■ $this titanium" }
                    building.cost.ergon?.apply { description += "\n■ $this ergon" }
                    building.cost.items?.apply {
                        for (item in this) {
                            description += "\n1 $item"
                        }
                    }
                    choose.visibility = View.VISIBLE
                    costs.text = description
                }
            }
        }
        findViewById<TextView>(R.id.cancelChoose).setBounceOnClick {
            dismiss()
        }
        setOnDismissListener {
            listener.onDismiss()
        }
    }

    interface ChooseBuildingDialogListener {
        fun onChooseBuilding(key: String)
        fun onDismiss() { }
    }
}
