package com.example.my_video_player.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.my_video_player.R
import com.example.my_video_player.classes.ProgressRequestBody
import com.example.my_video_player.classes.UpdateVideoFormData
import com.example.my_video_player.entities.CommonResponseEntity
import com.example.my_video_player.eventsEntities.ManuscriptRefreshEvent
import com.example.my_video_player.eventsEntities.VideoRefreshEvent
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import org.greenrobot.eventbus.EventBus

class EditVideoDialogFragment(
    private val id: String,
    private val videoTitle: String = "",
    private val onCloseButtonClickListener: View.OnClickListener
) : DialogFragment() {
    private lateinit var videoThumbnail: ImageView
    private val updateVideoFormData = UpdateVideoFormData()
    private lateinit var posterPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var uploadTint: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        posterPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedPosterUri: Uri? = data?.data
                    // 处理选中的视频URI
                    if (selectedPosterUri != null) {
                        // 例如：将视频URI传递给另一个组件或显示在UI中
                        handlePosterUri(selectedPosterUri)
                    }
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.CustomDialog) // 使用自定义样式
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_edit_video_dialog, null)
        dialog.setContentView(view)
        uploadTint = view.findViewById(R.id.upload_tint)
        val title = view.findViewById<EditText>(R.id.video_title)
        val closeBtn = view.findViewById<ImageView>(R.id.close_btn)
        title.setText(videoTitle)
        closeBtn.setOnClickListener {
            onCloseButtonClickListener.onClick(it)
            dialog.dismiss()
        }

        videoThumbnail = view.findViewById(R.id.video_thumbnail)
        videoThumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            posterPickerLauncher.launch(intent)
        }

        val confirmButton = view.findViewById<TextView>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            if (title.text.toString().isEmpty()) {
                NoticeDialogFragment("warning", "标题不能为空", "标题不能为空哦") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else if (updateVideoFormData.postUri == Uri.EMPTY) {
                NoticeDialogFragment("warning", "封面不能为空", "封面不能为空哦") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else {
                updateVideoFormData.title = title.text.toString()
                updateVideoFormData.id = id
                updateVideo()
            }
        }
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private fun handlePosterUri(selectedPosterUri: Uri) {
        videoThumbnail.setImageURI(selectedPosterUri)
        uploadTint.visibility = View.GONE
        updateVideoFormData.postUri = selectedPosterUri
    }

    private fun updateVideo() {
        val loadingDialog = LoadingDialogFragment("上传中：0%")
        loadingDialog.show(childFragmentManager, "loading_dialog")
        RetrofitUtil.updateVideo(
            updateVideoFormData,
            requireContext(),
            object : CallBackInfo<CommonResponseEntity> {
                override fun onSuccess(data: CommonResponseEntity) {
                    loadingDialog.dismiss()
                    if (data.success) {
                        NoticeDialogFragment("success", "修改结果", "修改成功！") {
                            dismiss()
                        }.show(
                            childFragmentManager,
                            "notice_dialog"
                        )
                        EventBus.getDefault().postSticky(ManuscriptRefreshEvent())
                        EventBus.getDefault().postSticky(VideoRefreshEvent())
                    } else {
                        NoticeDialogFragment(
                            "error",
                            "修改结果",
                            when (data.code) {
                                2 -> "修改失败！\n原因：未知错误"
                                3 -> "修改失败！\n原因：无法删除旧封面"
                                4 -> "修改失败！\n原因：无法保存新封面"
                                else -> "修改失败！\n原因：该视频不存在，请刷新后重试"
                            }
                        ) {
                            dismiss()
                        }.show(
                            childFragmentManager,
                            "notice_dialog"
                        )
                    }
                }

                override fun onFailure(code: Int, meg: String) {
                    NoticeDialogFragment("error", "修改结果", "修改失败！\n原因：$meg") {
                        dismiss()
                    }.show(
                        childFragmentManager,
                        "notice_dialog"
                    )
                }
            },
            object : ProgressRequestBody.ProgressListener {
                override fun onProgress(progress: Int) {
                    loadingDialog.setText("上传中：$progress%")
                }
            })
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框的宽度为 300dp
        dialog?.window?.setLayout(
            (300 * resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        onCloseButtonClickListener.onClick(view)
        super.onDismiss(dialog)
    }
}