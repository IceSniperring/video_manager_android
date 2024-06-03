package com.example.my_video_player.activities

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.VideoItemAdapter
import com.example.my_video_player.entities.HomePageRandomVideoEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoItemEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import okhttp3.internal.notifyAll

class HomePageActivity : AppCompatActivity() {
    private val BASE_URL = "http://192.168.31.200:10003"
    private var current = 1
    private lateinit var smartRefreshLayout: SmartRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val videoItemEntityEntityList: MutableList<VideoItemEntity> = mutableListOf()
        val videoInfoRecyclerView = findViewById<RecyclerView>(R.id.video_info_recycler_view)
        val videoInfoAdapter = VideoItemAdapter(videoItemEntityEntityList)
        videoInfoRecyclerView.layoutManager = GridLayoutManager(this, 2)
        videoInfoRecyclerView.adapter = videoInfoAdapter
        refresh(videoInfoAdapter, videoItemEntityEntityList)
        smartRefreshLayout = findViewById<SmartRefreshLayout>(R.id.refresh)
        smartRefreshLayout.setOnRefreshListener {
            refresh(videoInfoAdapter, videoItemEntityEntityList)
        }

        smartRefreshLayout.setOnLoadMoreListener {
            loadMore(videoInfoAdapter, videoItemEntityEntityList)
        }
    }

    fun refresh(
        videoInfoAdapter: VideoItemAdapter,
        videoItemEntityEntityList: MutableList<VideoItemEntity>
    ) {
        RetrofitUtil.getRandomVideo(this, object : CallBackInfo<HomePageRandomVideoEntity> {
            override fun onSuccess(data: HomePageRandomVideoEntity) {
                val records = data.records
                videoItemEntityEntityList.clear()
                videoInfoAdapter.notifyDataSetChanged()
                records.forEach {
                    RetrofitUtil.getUserInfo(
                        this@HomePageActivity,
                        object : CallBackInfo<UserEntity> {
                            override fun onSuccess(data: UserEntity) {
                                videoItemEntityEntityList.add(
                                    VideoItemEntity(
                                        it.id,
                                        it.title,
                                        "${BASE_URL}${it.postPath}",
                                        "${BASE_URL}${it.filePath}",
                                        it.uploadDate,
                                        data
                                    )
                                )
                                videoInfoAdapter.notifyItemInserted(videoItemEntityEntityList.size)
                            }

                            override fun onFailure(code: Int, meg: String) {}
                        },
                        it.uid
                    )
                    smartRefreshLayout.finishRefresh()
                }
            }

            override fun onFailure(code: Int, meg: String) {
            }
        }, 1)
        current = 1
    }

    fun loadMore(
        videoInfoAdapter: VideoItemAdapter,
        videoItemEntityEntityList: MutableList<VideoItemEntity>
    ) {
        RetrofitUtil.getRandomVideo(this, object : CallBackInfo<HomePageRandomVideoEntity> {
            override fun onSuccess(data: HomePageRandomVideoEntity) {
                current++
                if (current <= data.pages) {
                    Log.d("pages", data.pages.toString())
                    Log.d("current", current.toString())
                    val records = data.records
                    records.forEach {
                        RetrofitUtil.getUserInfo(
                            this@HomePageActivity,
                            object : CallBackInfo<UserEntity> {
                                override fun onSuccess(data: UserEntity) {
                                    videoItemEntityEntityList.add(
                                        VideoItemEntity(
                                            it.id,
                                            it.title,
                                            "${BASE_URL}${it.postPath}",
                                            "${BASE_URL}${it.filePath}",
                                            it.uploadDate,
                                            data
                                        )
                                    )
                                    videoInfoAdapter.notifyItemInserted(videoItemEntityEntityList.size)
                                }

                                override fun onFailure(code: Int, meg: String) {}
                            },
                            it.uid
                        )
                    }
                    smartRefreshLayout.finishLoadMore()
                } else {
                    Toast.makeText(this@HomePageActivity, "无更多视频", Toast.LENGTH_SHORT).show()
                    current--
                    smartRefreshLayout.finishLoadMore()
                }
            }

            override fun onFailure(code: Int, meg: String) {
            }
        }, current)
    }
}