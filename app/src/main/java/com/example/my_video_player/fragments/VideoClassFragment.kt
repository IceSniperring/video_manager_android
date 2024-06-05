package com.example.my_video_player.fragments

import android.os.Bundle
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

class VideoClassFragment : Fragment() {
    private val BASE_URL = "http://192.168.31.200:10003"
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
        val view = inflater.inflate(R.layout.fragment_viedo_class, container, false)
        setupViews(view)
        return view
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
                records.forEach {
                    videoItemEntityList.add(
                        VideoItemEntity(
                            it.id,
                            it.title,
                            "$BASE_URL${it.postPath}",
                            "$BASE_URL${it.filePath}",
                            it.uploadDate,
                            UserEntity(1, "ice", "", "")
                        )
                    )
                    videoInfoAdapter.notifyItemInserted(videoItemEntityList.size)
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
                current++
                if (current <= data.pages) {
                    val records = data.records
                    records.forEach {
                        videoItemEntityList.add(
                            VideoItemEntity(
                                it.id,
                                it.title,
                                "$BASE_URL${it.postPath}",
                                "$BASE_URL${it.filePath}",
                                it.uploadDate,
                                UserEntity(1, "ice", "", "")
                            )
                        )
                        videoInfoAdapter.notifyItemInserted(videoItemEntityList.size)
                    }
                    smartRefreshLayout.finishLoadMore()
                } else {
                    Toast.makeText(requireContext(), "无更多视频", Toast.LENGTH_SHORT).show()
                    current--
                    smartRefreshLayout.finishLoadMore()
                }
            }

            override fun onFailure(code: Int, msg: String) {}
        }, videoClass, current)
    }
}