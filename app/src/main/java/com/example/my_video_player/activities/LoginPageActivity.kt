package com.example.my_video_player.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RSAEncrypt
import com.example.my_video_player.utils.RetrofitUtil
import org.bouncycastle.jcajce.provider.asymmetric.RSA
import kotlin.math.log

class LoginPageActivity : AppCompatActivity() {
    private val BASE_URL = "http://192.168.31.200:10003"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginButton = findViewById<Button>(R.id.login_btn)
        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            login(LoginUserEntity(username, RSAEncrypt.encrypt(password)))
        }

    }

    fun login(loginUserEntity: LoginUserEntity) {
        RetrofitUtil.login(this@LoginPageActivity, object : CallBackInfo<LoginStatusEntity> {
            override fun onSuccess(data: LoginStatusEntity) {
                if (data.success) {
                    if (data.code == 1) {
                        Toast.makeText(this@LoginPageActivity, "登录成功", Toast.LENGTH_SHORT)
                            .show()
                        RetrofitUtil.getUserInfoByUsername(
                            this@LoginPageActivity,
                            object : CallBackInfo<UserEntity> {
                                override fun onSuccess(data: UserEntity) {

                                }

                                override fun onFailure(code: Int, meg: String) {

                                }

                            },
                            loginUserEntity.username
                        )
                    }
                } else {
                    if (data.code == 2) {
                        Toast.makeText(this@LoginPageActivity, "密码错误", Toast.LENGTH_SHORT)
                            .show()
                    } else if (data.code == 3) {
                        Toast.makeText(this@LoginPageActivity, "用户不存在", Toast.LENGTH_SHORT)
                            .show()
                    } else if (data.code == 4) {
                        Toast.makeText(this@LoginPageActivity, "未知错误", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(code: Int, meg: String) {

            }

        }, loginUserEntity)
    }
}