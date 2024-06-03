package com.example.my_video_player.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.VideoItemAdapter
import com.example.my_video_player.entities.HomePageRandomVideoEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoItemEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil

class PlayPageActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    val BASE_URL = "http://192.168.31.200:10003"
    private var doubleTapDetector: GestureDetector? = null
    private var currentPlaybackPosition: Long = 0L

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
        val mediaItem = MediaItem.Builder()
            .setUri(bundle?.getString("filePath") ?: "")
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Kono").build())
            .build()
        playerView.player = player
        playerView.useController = true
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        val title: TextView = playerView.findViewById(R.id.exo_text)
        val backButton: ImageView = playerView.findViewById(R.id.back_btn)
        val playButton: ImageView = playerView.findViewById(R.id.exo_play)
        val pauseButton: ImageView = playerView.findViewById(R.id.exo_pause)
        val fullScreenButton: ImageView = playerView.findViewById(R.id.fullscreen_btn)
        title.text = bundle?.getString("title")
        backButton.setOnClickListener {
            exitFullScreen()
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
                exitFullScreen()
                backButton.visibility = View.GONE
            } else {
                enterFullScreen()
                backButton.visibility = View.VISIBLE
            }
            fullScreenButton.isSelected = !fullScreenButton.isSelected
        }

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

        val videoItemEntityEntityList: MutableList<VideoItemEntity> = mutableListOf()
        val videoInfoRecyclerView = findViewById<RecyclerView>(R.id.video_recommend_recycler_view)
        val videoInfoAdapter = VideoItemAdapter(videoItemEntityEntityList)
        videoInfoRecyclerView.layoutManager = GridLayoutManager(this, 2)
        videoInfoRecyclerView.adapter = videoInfoAdapter

        RetrofitUtil.getRandomVideo(this, object : CallBackInfo<HomePageRandomVideoEntity> {
            override fun onSuccess(data: HomePageRandomVideoEntity) {
                val records = data.records
                records.forEach {
                    RetrofitUtil.getUserInfo(
                        this@PlayPageActivity,
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
            }

            override fun onFailure(code: Int, meg: String) {
            }
        }, 1)
    }

    //滑动手势
    private fun adjustPlaybackPosition(deltaX: Float) {
        val seekOffset = (deltaX * 3).toLong() // 根据滑动距离调整快进/快退幅度
        val newPosition = player.currentPosition + seekOffset
        player.seekTo(newPosition.coerceIn(0, player.duration))
    }

    //全屏以及退出全屏
    @RequiresApi(Build.VERSION_CODES.R)
    private fun enterFullScreen() {
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
    private fun exitFullScreen() {
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
}
