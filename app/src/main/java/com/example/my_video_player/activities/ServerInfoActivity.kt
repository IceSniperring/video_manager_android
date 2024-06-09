package com.example.my_video_player.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_video_player.R
import com.example.my_video_player.fragments.LoadingDialogFragment
import com.example.my_video_player.fragments.AlterDialogFragment
import com.example.my_video_player.fragments.NoticeDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.ConnectTestUtil
import com.example.my_video_player.utils.URLValidUtil
import com.tencent.mmkv.MMKV
import kotlin.system.exitProcess

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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish()
            }
        }

        val serverAddress: EditText = findViewById(R.id.server_address)
        val resourceAddress: EditText = findViewById(R.id.resource_address)
        val confirmButton: Button = findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener {
            //加载弹窗
            val loadingDialogFragment = LoadingDialogFragment("建立连接中...")

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
                    AlterDialogFragment("是否确认服务器信息", message,
                        {
                            loadingDialogFragment.show(supportFragmentManager, "")
                            ConnectTestUtil.testConnect(object : CallBackInfo<List<String>> {
                                override fun onSuccess(data: List<String>) {
                                    MMKV.defaultMMKV().encode("serverAddress", serverAddressString)
                                    MMKV.defaultMMKV()
                                        .encode("resourceAddress", resourceAddressString)
                                    loadingDialogFragment.dismiss()
                                    NoticeDialogFragment("连接成功", "恭喜设置成功！") {
                                        finish()
                                        if (intent.getBooleanExtra("isReConfig", false)) {
                                            val intent = Intent(
                                                this@ServerInfoActivity,
                                                MainActivity::class.java
                                            )
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            exitProcess(0)
                                        } else {
                                            val intent =
                                                Intent(
                                                    this@ServerInfoActivity,
                                                    MainActivity::class.java
                                                )
                                            startActivity(intent)
                                        }
                                    }.show(
                                        supportFragmentManager,
                                        "connect_result"
                                    )
                                }

                                override fun onFailure(code: Int, meg: String) {
                                    NoticeDialogFragment("连接失败", "原因:${meg}") {}.show(
                                        supportFragmentManager,
                                        "connect_result"
                                    )
                                    loadingDialogFragment.dismiss()
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