package com.example.my_video_player.activities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.DialogCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_video_player.R
import com.example.my_video_player.fragments.MyDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.ConnectTestUtil
import com.example.my_video_player.utils.RetrofitUtil
import com.example.my_video_player.utils.URLValidUtil
import com.tencent.mmkv.MMKV

class ServerInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_server_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (MMKV.defaultMMKV().decodeString("serverAddress") != null && MMKV.defaultMMKV()
                .decodeString("resourceAddress") != null
        ) {
            if (!intent.getBooleanExtra("isReConfig", false)) {
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        val serverAddress: EditText = findViewById(R.id.server_address)
        val resourceAddress: EditText = findViewById(R.id.resource_address)
        val confirmButton: Button = findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener {
            var serverAddressString = serverAddress.text.toString()
            var resourceAddressString = resourceAddress.text.toString()
            if (!serverAddressString.contains("://")) {
                serverAddressString = "http://$serverAddressString"
            }
            if (!resourceAddressString.contains("://")) {
                resourceAddressString = "http://$resourceAddressString"
            }
            if (
                !URLValidUtil.isValidUrl(serverAddressString)
                || !URLValidUtil.isValidUrl(resourceAddressString)
            ) {
                Toast.makeText(this, "请输入正确的URL", Toast.LENGTH_SHORT).show()
            } else {
                if (serverAddressString.isNotEmpty() && resourceAddressString.isNotEmpty()) {
                    val message = "后端地址:$serverAddressString\n资源地址:$resourceAddressString"
                    MyDialogFragment("是否确认服务器信息", message,
                        {
                            ConnectTestUtil.testConnect(object : CallBackInfo<List<String>> {
                                override fun onSuccess(data: List<String>) {
                                    Toast.makeText(
                                        this@ServerInfoActivity,
                                        "服务器连接成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    MMKV.defaultMMKV().encode("serverAddress", serverAddressString)
                                    MMKV.defaultMMKV()
                                        .encode("resourceAddress", resourceAddressString)
                                    finish()
                                    val intent =
                                        Intent(this@ServerInfoActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }

                                override fun onFailure(code: Int, meg: String) {
                                    Toast.makeText(
                                        this@ServerInfoActivity,
                                        meg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, serverAddressString)

                        },
                        {

                        }).show(supportFragmentManager, "dialog")
                }
            }
        }
    }
}