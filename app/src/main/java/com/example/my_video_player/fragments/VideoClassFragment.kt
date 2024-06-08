package com.example.my_video_player.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.VideoItemAdapter
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.VideoItemEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV

class VideoClassFragment : Fragment() {
    private val resourceAddress = MMKV.defaultMMKV().decodeString("resourceAddress")
    private val BASE_URL = resourceAddress ?: "http://192.168.31.200:10003"
    private var current = 1
    private lateinit var smartRefreshLayout: SmartRefreshLayout
    private val videoItemEntityList: MutableList<VideoItemEntity> = mutableListOf()
    private lateinit var videoInfoAdapter: VideoItemAdapter

    companion object {
        private const val ARG_VIDEO_CLASS = "arg_video_class"

        @JvmStatic
        fun newInstance(videoClass: String): VideoClassFragment {
            val fragment = VideoClassFragment()
            val args = Bundle()
            args.putString(ARG_VIDEO_CLASS, videoClass)
            fragment.arguments = args
            return fragment
        }
    }

    private val videoClass: String by lazy { arguments?.getString(ARG_VIDEO_CLASS) ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_viedo_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
    }

    private fun setupViews(view: View) {
        val videoClassRecyclerView = view.findViewById<RecyclerView>(R.id.video_class_recycler_view)
        videoInfoAdapter = VideoItemAdapter(videoItemEntityList)
        videoClassRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        videoClassRecyclerView.adapter = videoInfoAdapter

        smartRefreshLayout = view.findViewById(R.id.class_refresh)
        smartRefreshLayout.setOnRefreshListener { refresh() }
        smartRefreshLayout.setOnLoadMoreListener { loadMore() }

        refresh()
    }

    private fun refresh() {
        RetrofitUtil.getVideoByKind(requireContext(), object :
            CallBackInfo<VideoEntity> {
            override fun onSuccess(data: VideoEntity) {
                val records = data.records
                videoItemEntityList.clear()
                videoInfoAdapter.notifyDataSetChanged()
                records.forEachIndexed { index, it ->
                    videoItemEntityList.add(
                        VideoItemEntity(
                            it.id,
                            it.title,
                            "$BASE_URL${it.postPath}",
                            "$BASE_URL${it.filePath}",
                            it.uploadDate,
                            UserEntity(1, "", "", "")
                        )
                    )
                    videoInfoAdapter.notifyItemInserted(videoItemEntityList.size)
                    RetrofitUtil.getUserInfoById(
                        requireContext(),
                        object : CallBackInfo<UserEntity> {
                            override fun onSuccess(data: UserEntity) {
                                videoItemEntityList[index] = VideoItemEntity(
                                    it.id,
                                    it.title,
                                    "$BASE_URL${it.postPath}",
                                    "$BASE_URL${it.filePath}",
                                    it.uploadDate,
                                    UserEntity(it.uid, data.username, data.avatarPath, data.email)
                                )
                                videoInfoAdapter.notifyItemChanged(index)
                            }

                            override fun onFailure(code: Int, meg: String) {

                            }
                        },
                        it.uid,
                    )
                }
                smartRefreshLayout.finishRefresh()
                current++
            }

            override fun onFailure(code: Int, msg: String) {}
        }, videoClass, 1)
        current = 1
    }

    private fun loadMore() {
        RetrofitUtil.getVideoByKind(requireContext(), object :
            CallBackInfo<VideoEntity> {
            override fun onSuccess(data: VideoEntity) {
                if (current <= data.pages) {
                    val records = data.records
                    val nowSize = videoItemEntityList.size
                    records.forEachIndexed { index, it ->
                        videoItemEntityList.add(
                            VideoItemEntity(
                                it.id,
                                it.title,
                                "$BASE_URL${it.postPath}",
                                "$BASE_URL${it.filePath}",
                                it.uploadDate,
                                UserEntity(1, "", "", "")
                            )
                        )
                        videoInfoAdapter.notifyItemInserted(videoItemEntityList.size)
                        RetrofitUtil.getUserInfoById(
                            requireContext(),
                            object : CallBackInfo<UserEntity> {
                                override fun onSuccess(data: UserEntity) {
                                    videoItemEntityList[index + nowSize] = VideoItemEntity(
                                        it.id,
                                        it.title,
                                        "$BASE_URL${it.postPath}",
                                        "$BASE_URL${it.filePath}",
                                        it.uploadDate,
                                        UserEntity(
                                            it.uid,
                                            data.username,
                                            data.avatarPath,
                                            data.email
                                        )
                                    )
                                    videoInfoAdapter.notifyItemChanged(index + nowSize)
                                }

                                override fun onFailure(code: Int, meg: String) {

                                }
                            },
                            it.uid,
                        )
                    }
                    current++
                    smartRefreshLayout.finishLoadMore()
                } else {
                    Toast.makeText(requireContext(), "无更多视频", Toast.LENGTH_SHORT).show()
                    smartRefreshLayout.finishLoadMore()
                }
            }

            override fun onFailure(code: Int, msg: String) {}
        }, videoClass, current)
    }
}