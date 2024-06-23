package com.example.my_video_player.activities

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_video_player.R
import com.example.my_video_player.adapters.HistoryManageAdapter
import com.example.my_video_player.entities.HistoryVideoItemEntity
import com.example.my_video_player.eventsEntities.HistoryManageListEmptyEvent
import com.example.my_video_player.eventsEntities.VideoManageListEmptyEvent
import com.example.my_video_player.fragments.AlertDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryManageActivity : AppCompatActivity() {
    private lateinit var refreshLayout: SmartRefreshLayout

    override fun onResume() {
        super.onResume()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history_manage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backBtn = findViewById<ImageView>(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
        }
        EventBus.getDefault().register(this)
        val uid = MMKV.defaultMMKV().decodeLong("uid")
        if (uid == 0L) {
            AlertDialogFragment("要登陆吗？", SpannableString("请先登录后再查看历史哦！"), {
                val intent = Intent(this@HistoryManageActivity, LoginPageActivity::class.java)
                startActivity(intent)
            }, {
                finish()
            }).show(supportFragmentManager, "login_dialog")
        } else {
            refresh()
        }

        refreshLayout = findViewById(R.id.refresh_layout)

        refreshLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun refresh() {
        val recyclerView = findViewById<RecyclerView>(R.id.history_manage_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val uid = MMKV.defaultMMKV().decodeLong("uid")
        RetrofitUtil.getHistory(
            uid,
            this@HistoryManageActivity,
            object : CallBackInfo<List<HistoryVideoItemEntity>> {
                override fun onSuccess(data: List<HistoryVideoItemEntity>) {
                    val historyManageAdapter =
                        HistoryManageAdapter(data.toMutableList(), this@HistoryManageActivity)
                    recyclerView.adapter = historyManageAdapter
                    if (data.isNotEmpty()) {
                        val emptyArea = findViewById<ConstraintLayout>(R.id.empty_area)
                        emptyArea.visibility = View.GONE
                    } else {
                        EventBus.getDefault().postSticky(HistoryManageListEmptyEvent())
                    }

                    if (refreshLayout.isRefreshing) {
                        refreshLayout.finishRefresh()
                    }
                }

                override fun onFailure(code: Int, meg: String) {
                    Toast.makeText(this@HistoryManageActivity, "获取历史失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onListEmptyEvent(event: HistoryManageListEmptyEvent) {
        val emptyArea = findViewById<ConstraintLayout>(R.id.empty_area)
        emptyArea.alpha = 0f
        emptyArea.visibility = View.VISIBLE
        emptyArea.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}