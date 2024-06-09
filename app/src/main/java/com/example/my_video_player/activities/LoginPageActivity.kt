package com.example.my_video_player.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.my_video_player.R
import com.example.my_video_player.entities.LoginStatusEntity
import com.example.my_video_player.entities.LoginUserEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.eventsEntities.LoginEventEntity
import com.example.my_video_player.fragments.LoadingDialogFragment
import com.example.my_video_player.fragments.NoticeDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RSAEncrypt
import com.example.my_video_player.utils.RetrofitUtil
import com.tencent.mmkv.MMKV
import org.bouncycastle.jcajce.provider.asymmetric.RSA
import org.greenrobot.eventbus.EventBus
import kotlin.math.log

class LoginPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.username_area).text.toString()
            val password = findViewById<EditText>(R.id.password_area).text.toString()
            login(LoginUserEntity(username, RSAEncrypt.encrypt(password)))
        }

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    fun login(loginUserEntity: LoginUserEntity) {
        val loadingDialogFragment = LoadingDialogFragment("登陆中")
        loadingDialogFragment.show(
            supportFragmentManager,
            "loadingDialogFragment"
        )
        RetrofitUtil.login(this@LoginPageActivity, object : CallBackInfo<LoginStatusEntity> {
            override fun onSuccess(data: LoginStatusEntity) {
                if (data.success) {
                    if (data.code == 1) {
                        RetrofitUtil.getUserInfoByUsername(
                            this@LoginPageActivity,
                            object : CallBackInfo<UserEntity> {
                                override fun onSuccess(data: UserEntity) {
                                    loadingDialogFragment.dismiss()
                                    NoticeDialogFragment("登陆成功", "登陆成功，欢迎回来!") {
                                        EventBus.getDefault().postSticky(
                                            LoginEventEntity(
                                                data.username,
                                                data.avatarPath,
                                                data.id
                                            )
                                        )
                                        MMKV.defaultMMKV().encode("username", data.username)
                                        MMKV.defaultMMKV().encode("avatar", data.avatarPath)
                                        MMKV.defaultMMKV().encode("uid", data.id)
                                        finish()
                                        val intent =
                                            Intent(this@LoginPageActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }.show(
                                        supportFragmentManager,
                                        "noticeDialogFragment"
                                    )
                                }

                                override fun onFailure(code: Int, meg: String) {
                                    loadingDialogFragment.dismiss()
                                    NoticeDialogFragment("错误", "code:$code\n meg:$meg") {}.show(
                                        supportFragmentManager, "network_error"
                                    )
                                }

                            },
                            loginUserEntity.username
                        )
                    }
                } else {
                    loadingDialogFragment.dismiss()
                    if (data.code == 2) {
                        NoticeDialogFragment("密码错误", "密码错误，请重新输入") {}.show(
                            supportFragmentManager, "password_error"
                        )
                    } else if (data.code == 3) {
                        NoticeDialogFragment("用户不存在", "用户不存在，请重新输入") {}.show(
                            supportFragmentManager, "user_error"
                        )
                    } else if (data.code == 4) {
                        NoticeDialogFragment("未知错误", "未知错误！！！！") {}.show(
                            supportFragmentManager, "unknown_error"
                        )
                    }
                }
            }

            override fun onFailure(code: Int, meg: String) {
                loadingDialogFragment.dismiss()
                NoticeDialogFragment("错误", "code:$code\nmeg:$meg") {}.show(
                    supportFragmentManager, "network_error"
                )
            }

        }, loginUserEntity)
    }
}