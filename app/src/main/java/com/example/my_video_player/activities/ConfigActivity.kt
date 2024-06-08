package com.example.my_video_player.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_video_player.R
import com.example.my_video_player.eventsEntities.LogoutEventEntity
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val addressConfig = findViewById<ConstraintLayout>(R.id.config_item_address)
        addressConfig.setOnClickListener {
            val intent = Intent(this, ServerInfoActivity::class.java)
            intent.putExtra("isReConfig", true)
            startActivity(intent)
        }
        val logoutConfig = findViewById<ConstraintLayout>(R.id.logout_area)
        val loginConfig = findViewById<ConstraintLayout>(R.id.login_area)
        logoutConfig.visibility =
            if (MMKV.defaultMMKV().decodeString("username") != null) View.VISIBLE else View.GONE
        loginConfig.visibility =
            if (MMKV.defaultMMKV().decodeString("username") == null) View.VISIBLE else View.GONE
        logoutConfig.setOnClickListener {
            EventBus.getDefault().post(LogoutEventEntity())
            Toast.makeText(this, "退出登陆成功", Toast.LENGTH_SHORT).show()
            loginConfig.visibility = View.VISIBLE
            logoutConfig.visibility = View.GONE
        }
        loginConfig.setOnClickListener {
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
        }
    }
}