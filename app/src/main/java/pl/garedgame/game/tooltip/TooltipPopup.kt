package pl.garedgame.game.tooltip

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import pl.garedgame.game.GameApplication
import pl.garedgame.game.util.Utils

open class TooltipPopup(resLayoutId: Int) : PopupWindow() {

    var onDismiss: (() -> Unit)? = null

    init {
        contentView = LayoutInflater.from(GameApplication.instance).inflate(resLayoutId, null)
        contentView.setOnTouchListener { _, _ ->
            dismiss()
            true
        }
        setOnDismissListener {
            onDismiss?.invoke()
        }
    }

    fun showAt(anchor: View, gravity: Int) {
        Utils.handler.post {
            showAsDropDown(anchor,
                    when {
                        gravity and Gravity.START == Gravity.START -> -width
                        gravity and Gravity.END == Gravity.END -> anchor.width
                        else -> anchor.width / 2 - width / 2
                    },
                    (when {
                        gravity and Gravity.TOP == Gravity.TOP -> -height - anchor.height
                        gravity and Gravity.BOTTOM == Gravity.BOTTOM -> 0
                        else -> -height / 2 - anchor.height / 2
                    }))
        }
    }
}
