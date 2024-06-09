package com.example.my_video_player.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.my_video_player.R

class NoticeDialogFragment(
    private val title: String,
    private val message: String,
    private val onClickListener: View.OnClickListener,

    ) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.CustomDialog) // 使用自定义样式
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_notice_dialog, null)
        dialog.setContentView(view)

        val titleTextView = view.findViewById<TextView>(R.id.notice_dialog_title)
        val messageTextView = view.findViewById<TextView>(R.id.notice_dialog_message)
        val dialogButton = view.findViewById<TextView>(R.id.dialog_button)

        titleTextView.text = this.title
        messageTextView.text = this.message
        dialogButton.setOnClickListener {
            onClickListener.onClick(it)
            dismiss()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框的宽度为 268dp
        dialog?.window?.setLayout(
            (268 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        onClickListener.onClick(view)
        super.onDismiss(dialog)
    }
}