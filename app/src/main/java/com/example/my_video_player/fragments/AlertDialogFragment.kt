package com.example.my_video_player.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.my_video_player.R

class AlertDialogFragment(
    private val title: String,
    private val message: SpannableString,
    private val onPositiveButtonClickListener: View.OnClickListener,
    private val onNegativeButtonClickListener: View.OnClickListener
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.CustomDialog) // 使用自定义样式
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_alert_dialog, null)
        dialog.setContentView(view)

        val titleTextView = view.findViewById<TextView>(R.id.alert_dialog_title)
        val messageTextView = view.findViewById<TextView>(R.id.alert_dialog_message)
        val cancelButton = view.findViewById<TextView>(R.id.my_dialog_cancel)
        val confirmButton = view.findViewById<TextView>(R.id.my_dialog_confirm)

        titleTextView.text = this.title
        messageTextView.text = this.message
        confirmButton.setOnClickListener {
            onPositiveButtonClickListener.onClick(it)
            dismiss()
        }
        cancelButton.setOnClickListener {
            onNegativeButtonClickListener.onClick(it)
            dismiss()
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框的宽度为 268dp
        dialog?.window?.setLayout(
            (268 * resources.displayMetrics.density).toInt(),
            LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        onNegativeButtonClickListener.onClick(view)
        super.onDismiss(dialog)
    }
}