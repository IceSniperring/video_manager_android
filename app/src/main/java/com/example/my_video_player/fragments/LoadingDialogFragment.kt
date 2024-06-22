package com.example.my_video_player.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.my_video_player.R


class LoadingDialogFragment(private val loadingText: String) : DialogFragment() {
    private var animation: Animation? = null
    private lateinit var loadingIcon: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(),R.style.CustomDialog) // 使用自定义样式
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_loading_dialog, null)
        dialog.setContentView(view)
        val loadingTextView= view.findViewById<TextView>(R.id.loading_text)
        loadingTextView.text = loadingText

        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    fun setText(loadingText: String){
        val loadingTextView= dialog?.findViewById<TextView>(R.id.loading_text)
        loadingTextView?.text = loadingText
    }
    override fun onStart() {
        super.onStart()
        // 设置对话框的宽度为 268dp
        dialog?.window?.setLayout(
            (120 * resources.displayMetrics.density).toInt(),
            (120 * resources.displayMetrics.density).toInt()
        )
    }
}