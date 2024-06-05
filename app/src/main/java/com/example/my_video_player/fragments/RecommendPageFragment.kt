package com.example.my_video_player.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.VideoItemAdapter
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoItemEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class RecommendPageFragment : Fragment() {
    private val BASE_URL = "http://192.168.31.200:10003"
    private var current = 1
    private lateinit var smartRefreshLayout: SmartRefreshLayout
    private val videoItemEntityList: MutableList<VideoItemEntity> = mutableListOf()
    private lateinit var videoInfoAdapter: VideoItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend_page, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val videoInfoRecyclerView = view.findViewById<RecyclerView>(R.id.video_info_recycler_view)
        videoInfoAdapter = VideoItemAdapter(videoItemEntityList)
        videoInfoRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        videoInfoRecyclerView.adapter = videoInfoAdapter

        smartRefreshLayout = view.findViewById(R.id.refresh)
        smartRefreshLayout.setOnRefreshListener { refresh() }
        smartRefreshLayout.setOnLoadMoreListener { loadMore() }

        refresh()
    }

    private fun refresh() {
        RetrofitUtil.getRandomVideo(requireContext(), object :
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
        }, 1)
        current = 1
    }

    private fun loadMore() {
        RetrofitUtil.getRandomVideo(requireContext(), object :
            CallBackInfo<VideoEntity> {
            override fun onSuccess(data: VideoEntity) {
                current++
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
            }

            override fun onFailure(code: Int, msg: String) {}
        }, current)
    }
}