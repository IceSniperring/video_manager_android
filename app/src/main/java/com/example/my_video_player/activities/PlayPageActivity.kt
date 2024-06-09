package com.example.my_video_player.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.my_video_player.R
import com.example.my_video_player.adapters.PlayerPageVideoItemAdapter
import com.example.my_video_player.adapters.VideoItemAdapter
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.VideoItemEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PlayPageActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private val resourceAddress = MMKV.defaultMMKV().decodeString("resourceAddress")
    private val BASE_URL = resourceAddress ?: "http://192.168.31.200:10003"
    private var doubleTapDetector: GestureDetector? = null
    private var currentPlaybackPosition: Long = 0L
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var current = 1
    private lateinit var smartRefreshLayout: SmartRefreshLayout
    private val videoItemEntityList: MutableList<VideoItemEntity> = mutableListOf()
    private lateinit var videoInfoAdapter: PlayerPageVideoItemAdapter

    @RequiresApi(Build.VERSION_CODES.R)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setPreferredTextLanguage("zh-CN")) // 设置首选的文本语言为简体中文
        }
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        val bundle = intent.extras
        val playerView = findViewById<PlayerView>(R.id.player_view)
        val authorImage = findViewById<ImageFilterView>(R.id.author_image)
        val timeBar = findViewById<DefaultTimeBar>(R.id.time_bar)
        timeBar.setEnabled(false);
        Glide.with(this).load(bundle?.getString("authorImage") ?: "").into(authorImage)
        val authorName = findViewById<TextView>(R.id.author_name)
        authorName.text = bundle?.getString("authorName")
        val videoTitle = findViewById<TextView>(R.id.video_title)
        videoTitle.text = bundle?.getString("title")
        val uploadDate = findViewById<TextView>(R.id.upload_date)
        uploadDate.text = bundle?.getString("uploadDate")
        val mediaItem = MediaItem.Builder()
            .setUri(bundle?.getString("filePath") ?: "")
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Kono").build())
            .build()
        playerView.player = player
        playerView.useController = true
        player.setMediaItem(mediaItem)
        player.prepare()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val duration = player.duration //总时长
                    val currentPosition = player.currentPosition //当前播放位置
                    timeBar.setDuration(duration)
                    timeBar.setPosition(currentPosition)
                }
            }
        })
        //每秒更新时间
        coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                val duration = player.duration //总时长
                val currentPosition = player.currentPosition //当前播放位置
                if (player.isPlaying) {
                    timeBar.setDuration(duration)
                    timeBar.setPosition(currentPosition)
                }
            }
        }
        player.play()

        val title: TextView = playerView.findViewById(R.id.exo_text)
        val backButton: ImageView = playerView.findViewById(R.id.back_btn)
        val playButton: ImageView = playerView.findViewById(R.id.exo_play)
        val pauseButton: ImageView = playerView.findViewById(R.id.exo_pause)
        val fullScreenButton: ImageView = playerView.findViewById(R.id.fullscreen_btn)
        title.text = bundle?.getString("title")
        backButton.setOnClickListener {
            exitFullScreen(playerView)
            fullScreenButton.isSelected = false
            backButton.visibility = View.GONE
        }

        playButton.setOnClickListener {
            player.play()
            pauseButton.visibility = View.VISIBLE
            playButton.visibility = View.GONE
        }

        pauseButton.setOnClickListener {
            player.pause()
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.GONE
        }

        fullScreenButton.setOnClickListener {
            if (fullScreenButton.isSelected) {
                exitFullScreen(playerView)
                backButton.visibility = View.GONE
            } else {
                enterFullScreen(playerView)
                backButton.visibility = View.VISIBLE
                timeBar.visibility = View.GONE
            }
            fullScreenButton.isSelected = !fullScreenButton.isSelected
        }

        //控制器是否可视监听器
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                if (!fullScreenButton.isSelected) {
                    timeBar.visibility = View.GONE
                }
            } else if (visibility == View.GONE) {
                if (!fullScreenButton.isSelected) {
                    timeBar.visibility = View.VISIBLE
                }
            }
        })

        doubleTapDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (player.isPlaying) {
                        player.pause()
                        playButton.visibility = View.VISIBLE
                        pauseButton.visibility = View.GONE
                    } else {
                        player.play()
                        pauseButton.visibility = View.VISIBLE
                        playButton.visibility = View.GONE
                    }
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (playerView.isControllerFullyVisible)
                        playerView.showController()
                    else
                        playerView.hideController()
                    return false
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    playerView.showController()
                    e1?.let {
                        val deltaX = e2.x.minus(e1.x)
                        adjustPlaybackPosition(deltaX)
                    }

                    return true
                }
            })

        playerView.setOnTouchListener { v, event ->
            doubleTapDetector?.onTouchEvent(event) ?: false
        }

        //关闭动画
        playerView.setControllerAnimationEnabled(false)

        val videoInfoRecyclerView = findViewById<RecyclerView>(R.id.video_recommend_recycler_view)
        videoInfoAdapter = PlayerPageVideoItemAdapter(videoItemEntityList)
        videoInfoRecyclerView.layoutManager = GridLayoutManager(this, 1)
        videoInfoRecyclerView.adapter = videoInfoAdapter
        smartRefreshLayout = findViewById<SmartRefreshLayout>(R.id.player_page_refresh)
        smartRefreshLayout.setOnLoadMoreListener {
            loadMore()
        }
        loadMore()
    }

    //滑动手势
    private fun adjustPlaybackPosition(deltaX: Float) {
        val seekOffset = (deltaX * 3).toLong() // 根据滑动距离调整快进/快退幅度
        val newPosition = player.currentPosition + seekOffset
        player.seekTo(newPosition.coerceIn(0, player.duration))
    }

    //全屏以及退出全屏
    @RequiresApi(Build.VERSION_CODES.R)
    private fun enterFullScreen(playerView: PlayerView) {
        val layoutParams: ViewGroup.LayoutParams = playerView.layoutParams
        // 设置新的高度
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        // 重新应用修改后的 LayoutParams
        playerView.setLayoutParams(layoutParams)
        currentPlaybackPosition = player.currentPosition
        Log.d("ice", currentPlaybackPosition.toString())
        val controller = window.insetsController
        controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller?.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        player.seekTo(currentPlaybackPosition)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun exitFullScreen(playerView: PlayerView) {
        val layoutParams: ViewGroup.LayoutParams = playerView.layoutParams
        // 设置新的高度
        layoutParams.height = (layoutParams.width / 16) * 9
        // 重新应用修改后的 LayoutParams
        playerView.setLayoutParams(layoutParams)
        currentPlaybackPosition = player.currentPosition
        Log.d("ice", currentPlaybackPosition.toString())
        val controller = window.insetsController
        controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        player.seekTo(currentPlaybackPosition)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val controller = window.insetsController
            controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
        player.seekTo(currentPlaybackPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onRestart() {
        super.onRestart()
        player.play()
    }

    private fun loadMore() {
        RetrofitUtil.getRandomVideo(this, object :
            CallBackInfo<VideoEntity> {
            override fun onSuccess(data: VideoEntity) {
                current++
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
                        this@PlayPageActivity,
                        object : CallBackInfo<UserEntity> {
                            override fun onSuccess(data: UserEntity) {
                                videoItemEntityList[index + nowSize] = VideoItemEntity(
                                    it.id,
                                    it.title,
                                    "$BASE_URL${it.postPath}",
                                    "$BASE_URL${it.filePath}",
                                    it.uploadDate,
                                    UserEntity(it.uid, data.username, data.avatarPath, data.email)
                                )
                                videoInfoAdapter.notifyItemChanged(index + nowSize)
                            }

                            override fun onFailure(code: Int, meg: String) {

                            }
                        },
                        it.uid,
                    )
                }
                smartRefreshLayout.finishLoadMore()
            }

            override fun onFailure(code: Int, msg: String) {}
        }, current)
    }
}
