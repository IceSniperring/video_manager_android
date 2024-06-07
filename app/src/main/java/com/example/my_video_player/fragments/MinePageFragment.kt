package com.example.my_video_player.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.my_video_player.R
import com.example.my_video_player.activities.LoginPageActivity
import com.example.my_video_player.eventsEntities.LoginEventEntity
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MinePageFragment : Fragment() {
    private val BASE_URL = "http://192.168.31.200:10003"
    private lateinit var avatar: ImageFilterView
    private lateinit var username: TextView
    private lateinit var uid: TextView
    private lateinit var logout: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mine_page, container, false)
        avatar = view.findViewById<ImageFilterView>(R.id.avatar)
        username = view.findViewById<TextView>(R.id.username)
        uid = view.findViewById<TextView>(R.id.uid)
        logout = view.findViewById<TextView>(R.id.logout)

        if (MMKV.defaultMMKV().decodeString("username") != null) {
            Glide.with(requireContext())
                .load("$BASE_URL${MMKV.defaultMMKV().decodeString("avatar")}")
                .into(avatar)
            avatar.isEnabled = false
            username.text = MMKV.defaultMMKV().decodeString("username")
            uid.text = "uid:${MMKV.defaultMMKV().decodeLong("uid")}"
            uid.visibility = View.VISIBLE
            logout.visibility = View.VISIBLE
        }
        avatar.setOnClickListener {
            val intent = Intent(requireContext(), LoginPageActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
            MMKV.defaultMMKV().clearAll()
            avatar.isEnabled = true
            avatar.setImageResource(R.color.white)
            username.text = "未登录"
            uid.visibility = View.GONE
            logout.visibility = View.GONE
            startActivity(Intent(requireContext(), LoginPageActivity::class.java))
        }
        EventBus.getDefault().register(this)
        return view
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun getLoginEventEntity(loginEventEntity: LoginEventEntity) {
        Glide.with(requireContext()).load("$BASE_URL${loginEventEntity.avatar}")
            .into(avatar)
        logout.visibility = View.VISIBLE
        avatar.isEnabled = false
        username.text = loginEventEntity.username
        uid.text = "uid:${loginEventEntity.uid}"
        uid.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
