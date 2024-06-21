package com.example.my_video_player.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.TintInfo
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.example.my_video_player.R
import com.example.my_video_player.activities.LoginPageActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tencent.mmkv.MMKV


class UploadPageFragment : Fragment() {
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var posterPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoThumbnail: ImageFilterView
    private lateinit var uploadTint: LinearLayout
    private lateinit var videoPoster: TextView
    private lateinit var confirmBtn: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

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

    private fun handlePosterUri(selectedPosterUri: Uri) {
        videoThumbnail.setImageURI(selectedPosterUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_page, container, false)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav_menu)
        videoThumbnail = view.findViewById(R.id.video_thumbnail)
        uploadTint = view.findViewById(R.id.upload_tint)
        videoPoster = view.findViewById(R.id.video_poster)
        confirmBtn = view.findViewById(R.id.confirm_button)
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
        return view
    }

    private fun handleVideoUri(selectedVideoUri: Uri) {
        val thumbnail = getVideoThumbnail(selectedVideoUri)
        videoThumbnail.setImageBitmap(thumbnail)
        uploadTint.visibility = View.GONE
        videoPoster.visibility = View.VISIBLE
        confirmBtn.visibility = View.VISIBLE
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
}