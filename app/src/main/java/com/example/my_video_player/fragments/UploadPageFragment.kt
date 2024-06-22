package com.example.my_video_player.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.my_video_player.entities.UploadVideoFormData
import com.example.my_video_player.eventsEntities.KindRefreshEvent
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
    private val handler = Handler(Looper.getMainLooper())
    private val uploadVideoFormData: UploadVideoFormData = UploadVideoFormData()

    override fun onResume() {
        super.onResume()
        val username = MMKV.defaultMMKV().decodeString("username")
        if (username == null) {
            AlterDialogFragment("要登陆吗？", "请先登录后再上传视频", {
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
        val title = view.findViewById<TextView>(R.id.video_title)
        val kind = view.findViewById<TextView>(R.id.video_kind)
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
            if (uploadVideoFormData.videoUri == Uri.EMPTY) {
                NoticeDialogFragment("警告！", "请选择视频") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else if (kind.text.toString().isEmpty()) {
                NoticeDialogFragment("警告！", "请填写视频分类") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else if (title.text.toString().isEmpty()) {
                NoticeDialogFragment("警告！", "请填写视频标题") {}.show(
                    childFragmentManager,
                    "notice_dialog"
                )
            } else {
                uploadVideoFormData.title = title.text.toString()
                uploadVideoFormData.kind = kind.text.toString()
                uploadVideo()
            }
        }
        uploadVideoFormData.uid = MMKV.defaultMMKV().decodeLong("uid").toString() ?: ""
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
        uploadProgressArea.visibility = View.VISIBLE
        RetrofitUtil.uploadVideo(
            uploadVideoFormData,
            requireContext(),
            object : CallBackInfo<UploadResponseEntity> {
                override fun onSuccess(data: UploadResponseEntity) {
                    Toast.makeText(requireContext(), "上传成功", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().postSticky(KindRefreshEvent())
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
}