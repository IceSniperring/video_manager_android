package com.example.my_video_player.activities

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.VideoManagerAdapter
import com.example.my_video_player.entities.VideoInfoEntity
import com.example.my_video_player.eventsEntities.ListEmptyEvent
import com.example.my_video_player.eventsEntities.ManuscriptRefreshEvent
import com.example.my_video_player.fragments.AlertDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VideoManageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_manage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        EventBus.getDefault().register(this)
        val uid = MMKV.defaultMMKV().decodeLong("uid")
        if (uid == 0L) {
            AlertDialogFragment("要登陆吗？", SpannableString("请先登录后再管理稿件哦！"), {
                val intent = Intent(this@VideoManageActivity, LoginPageActivity::class.java)
                startActivity(intent)
            }, {
                finish()
            }).show(supportFragmentManager, "login_dialog")
        } else {
            initView()
        }
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.video_manage_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val uid = MMKV.defaultMMKV().decodeLong("uid")
        RetrofitUtil.getVideoByUid(
            uid,
            object : CallBackInfo<List<VideoInfoEntity>> {
                override fun onSuccess(data: List<VideoInfoEntity>) {
                    if (data.isNotEmpty()) {
                        val emptyArea = findViewById<ConstraintLayout>(R.id.empty_area)
                        emptyArea.visibility = View.GONE
                        val videoManageAdapter =
                            VideoManagerAdapter(data.toMutableList(), this@VideoManageActivity)
                        recyclerView.adapter = videoManageAdapter
                        recyclerView.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(code: Int, meg: String) {
                    Toast.makeText(this@VideoManageActivity, "获取稿件失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onListEmptyEvent(event: ListEmptyEvent) {
        val emptyArea = findViewById<ConstraintLayout>(R.id.empty_area)
        emptyArea.alpha = 0f
        emptyArea.visibility = View.VISIBLE
        emptyArea.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
        val recyclerView = findViewById<RecyclerView>(R.id.video_manage_recyclerView)
        recyclerView.visibility = View.GONE
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onManuscriptRefreshEvent(event: ManuscriptRefreshEvent) {
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}