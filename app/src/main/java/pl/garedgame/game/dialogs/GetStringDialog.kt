package pl.garedgame.game.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_get_string.*
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick

class GetStringDialog(context: Context, listener: GetStringDialogListener) : Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_get_string)
        description.text = listener.getDescription()
        edit.setText(listener.getCurrentString())

        ok.setBounceOnClick {
            val string = edit.text.toString()
            listener.onString(string)
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

    interface GetStringDialogListener {
        fun getDescription(): String = ""
        fun onString(string: String)
        fun getCurrentString(): String = ""
        fun onDismiss() { }
    }
}
