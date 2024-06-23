package com.example.my_video_player.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.my_video_player.R
import com.example.my_video_player.activities.LoginPageActivity
import com.example.my_video_player.classes.ProgressRequestBody
import com.example.my_video_player.entities.UploadResponseEntity
import com.example.my_video_player.classes.UploadVideoFormData
import com.example.my_video_player.eventsEntities.KindRefreshEvent
import com.example.my_video_player.eventsEntities.VideoRefreshEvent
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import java.io.File


class UploadPageFragment : Fragment() {
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var posterPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoThumbnail: ImageFilterView
    private lateinit var uploadTint: LinearLayout
    private lateinit var videoPoster: TextView
    private lateinit var confirmBtn: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var uploadProgress: ProgressBar
    private lateinit var uploadProgressArea: LinearLayout
    private lateinit var uploadProgressText: TextView
    private lateinit var videoTitle: EditText
    private lateinit var videoKind: EditText
    private val handler = Handler(Looper.getMainLooper())
    private val uploadVideoFormData: UploadVideoFormData = UploadVideoFormData()

    override fun onResume() {
        super.onResume()
        val username = MMKV.defaultMMKV().decodeString("username")
        if (username == null) {
            AlertDialogFragment("要登陆吗？", SpannableString("请先登录后再上传视频"), {
                val intent = Intent(requireContext(), LoginPageActivity::class.java)
                startActivity(intent)
            }, {
                bottomNavigationView.selectedItemId = R.id.home_page
            }).show(childFragmentManager, "login_dialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedVideoUri: Uri? = data?.data
                    // 处理选中的视频URI
                    if (selectedVideoUri != null) {
                        // 例如：将视频URI传递给另一个组件或显示在UI中
                        handleVideoUri(selectedVideoUri)
                    }
                }
            }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_page, container, false)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav_menu)
        videoThumbnail = view.findViewById(R.id.video_thumbnail)
        uploadProgressArea = view.findViewById(R.id.upload_progress_area)
        uploadProgress = view.findViewById(R.id.upload_progress)
        uploadProgressText = view.findViewById(R.id.upload_progress_text)
        uploadTint = view.findViewById(R.id.upload_tint)
        videoPoster = view.findViewById(R.id.video_poster)
        confirmBtn = view.findViewById(R.id.confirm_button)
        videoTitle = view.findViewById(R.id.video_title)
        videoKind = view.findViewById(R.id.video_kind)
        videoThumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("video/*")
            videoPickerLauncher.launch(intent)
        }
        videoPoster.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            posterPickerLauncher.launch(intent)
        }
        confirmBtn.setOnClickListener {
            uploadProgress.progress = 0
            uploadProgressText.text = "上传进度：0%"
            if (uploadVideoFormData.videoUri == Uri.EMPTY) {
                NoticeDialogFragment("warning", "警告！", "请选择视频") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else if (videoKind.text.toString().isEmpty()) {
                NoticeDialogFragment("warning", "警告！", "请填写视频分类") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else if (videoTitle.text.toString().isEmpty()) {
                NoticeDialogFragment("warning", "警告！", "请填写视频标题") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else {
                val message = "视频名称：${videoTitle.text}\n分类：${videoKind.text}"
                val spannable = SpannableString(message)
                val titleTextStart = message.indexOf(videoTitle.text.toString())
                val titleTextEnd = titleTextStart + videoTitle.text.length
                val kindTextStart = message.indexOf(videoKind.text.toString())
                val kindTextEnd = kindTextStart + videoKind.text.length
                // 设置颜色
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#55C8F6")),
                    titleTextStart,
                    titleTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#55C8F6")),
                    kindTextStart,
                    kindTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                AlertDialogFragment("确定上传吗？", spannable, {
                    uploadVideoFormData.title = videoTitle.text.toString()
                    uploadVideoFormData.kind = videoKind.text.toString()
                    uploadVideoFormData.uid = MMKV.defaultMMKV().decodeLong("uid").toString() ?: ""
                    uploadVideo()
                }, {}).show(childFragmentManager, "alert_dialog")
            }
        }
        return view
    }

    private fun handleVideoUri(selectedVideoUri: Uri) {
        val thumbnail = getVideoThumbnail(selectedVideoUri)
        videoThumbnail.setImageBitmap(thumbnail)
        if (thumbnail != null) {
            videoThumbnail.setImageBitmap(thumbnail)
            val thumbnailUri = saveBitmapToFile(thumbnail, requireContext())
            uploadVideoFormData.postUri = thumbnailUri
        }
        uploadTint.visibility = View.GONE
        videoPoster.visibility = View.VISIBLE
        confirmBtn.visibility = View.VISIBLE
        videoTitle.setText(getFileNameFromUri(requireContext(), selectedVideoUri))
        uploadVideoFormData.videoUri = selectedVideoUri
    }

    private fun handlePosterUri(selectedPosterUri: Uri) {
        videoThumbnail.setImageURI(selectedPosterUri)
        uploadVideoFormData.postUri = selectedPosterUri
    }

    private fun getVideoThumbnail(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(requireContext(), uri)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    private fun uploadVideo() {
        Toast.makeText(requireContext(), "开始上传", Toast.LENGTH_SHORT).show()
        uploadProgressArea.visibility = View.VISIBLE
        videoPoster.isEnabled = false
        videoPoster.alpha = 0.5f
        confirmBtn.isEnabled = false
        confirmBtn.alpha = 0.5f
        confirmBtn.text = "上传中"
        videoKind.isEnabled = false
        videoTitle.isEnabled = false
        val badge = bottomNavigationView.getOrCreateBadge(R.id.upload_page)
        badge.setTextAppearance(R.style.BadgeTextAppearance)
        badge.text = "上传中"
        RetrofitUtil.uploadVideo(
            uploadVideoFormData,
            requireContext(),
            object : CallBackInfo<UploadResponseEntity> {
                override fun onSuccess(data: UploadResponseEntity) {
                    Toast.makeText(requireContext(), "上传成功", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().postSticky(KindRefreshEvent())
                    EventBus.getDefault().postSticky(VideoRefreshEvent())
                }

                override fun onFailure(code: Int, meg: String) {
                    Toast.makeText(requireContext(), "上传失败:$code", Toast.LENGTH_SHORT).show()
                }
            }, object : ProgressRequestBody.ProgressListener {
                override fun onProgress(progress: Int) {
                    handler.post {
                        uploadProgress.progress = progress
                        uploadProgressText.text = "上传进度：$progress%"
                    }
                    if (progress == 100) {
                        uploadProgressArea.visibility = View.INVISIBLE
                        handler.post {
                            videoPoster.isEnabled = true
                            videoPoster.alpha = 1f
                            confirmBtn.isEnabled = true
                            confirmBtn.alpha = 1f
                            confirmBtn.text = "确认提交"
                            videoThumbnail.setImageDrawable(null)
                            uploadTint.visibility = View.VISIBLE
                            videoKind.setText("")
                            videoTitle.setText("")
                            videoKind.isEnabled = true
                            videoTitle.isEnabled = true
                            videoPoster.visibility = View.INVISIBLE
                            confirmBtn.visibility = View.INVISIBLE
                            uploadVideoFormData.videoUri = Uri.EMPTY
                            uploadVideoFormData.postUri = Uri.EMPTY
                        }
                        bottomNavigationView.removeBadge(R.id.upload_page)
                    }
                }
            })
    }

    //bitmap转file的uri
    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): Uri {
        val file = File(context.cacheDir, "thumbnail_${System.currentTimeMillis()}.png")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        // 去除文件后缀名
        result = result!!.substringBeforeLast('.')
        return result!!
    }
}