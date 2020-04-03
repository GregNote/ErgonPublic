package pl.garedgame.game.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_choose.*
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick

class ChooseDialog(context: Context, listener: ChooseDialogListener) : Dialog(context) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_choose)
        description.text = listener.getDescription()

        option1.text = listener.getOption1()
        option1.setBounceOnClick {
            listener.onOption1()
            setOnDismissListener(null)
            dismiss()
        }

        option2.text = listener.getOption2()
        option2.setBounceOnClick {
            listener.onOption2()
            setOnDismissListener(null)
            dismiss()
        }
        setOnDismissListener {
            listener.onDismiss()
        }
    }

    interface ChooseDialogListener {
        fun getDescription(): String
        fun getOption1(): String
        fun getOption2(): String
        fun onOption1()
        fun onOption2()
        fun onDismiss() { }
    }
}
