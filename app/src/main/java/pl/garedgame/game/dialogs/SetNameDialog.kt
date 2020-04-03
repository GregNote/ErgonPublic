package pl.garedgame.game.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_get_string.*
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick

class SetNameDialog(context: Context, listener: SetNameDialogListener) : Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_get_string)
        description.text = context.getString(R.string.setNameDescription)
        edit.setText(listener.getCurrentName())
        ok.setBounceOnClick {
            val name = edit.text.toString()
            listener.onSetName(name)
            setOnDismissListener(null)
            dismiss()
        }
        cancel.setBounceOnClick {
            dismiss()
        }
        setOnDismissListener {
            listener.onDismiss()
        }
    }

    interface SetNameDialogListener {
        fun onSetName(name: String)
        fun getCurrentName(): String = ""
        fun onDismiss() { }
    }
}
