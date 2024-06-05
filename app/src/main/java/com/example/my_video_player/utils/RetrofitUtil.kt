package com.example.my_video_player.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.my_video_player.entities.LoginStatusEntity
import com.example.my_video_player.entities.LoginUserEntity
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.interceptors.LoggerInterceptor
import com.example.my_video_player.interfaces.ApiService
import com.example.my_video_player.interfaces.CallBackInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtil {
    private const val BASE_URL = "http://192.168.31.200:10001"
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(LoggerInterceptor()).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val handler = Handler(Looper.getMainLooper())
    private val apiService = retrofit.create(ApiService::class.java)
    private const val FAILURE_MEG = "请求失败"


    fun getRandomVideo(
        context: Context,
        callBackInfo: CallBackInfo<VideoEntity>,
        current: Int = 1
    ) {
        val getRandomVideoApi = apiService.getHomeRandomVideo(current)
        getRandomVideoApi.enqueue(MyCallback(callBackInfo, context))
    }

    fun getUserInfoById(context: Context, callBackInfo: CallBackInfo<UserEntity>, id: Long = 1) {
        val getUserInfoApi = apiService.getUserById(id)
        getUserInfoApi.enqueue(MyCallback(callBackInfo, context))
    }

    fun getVideoByKind(
        context: Context,
        callBackInfo: CallBackInfo<VideoEntity>,
        kind: String,
        page: Int = 1
    ) {
        val getVideoByKindApi = apiService.getVideoByKind(kind, page)
        getVideoByKindApi.enqueue(MyCallback(callBackInfo, context))
    }

    fun getKind(context: Context, callBackInfo: CallBackInfo<List<String>>) {
        val getKindApi = apiService.getKind()
        getKindApi.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                val responseCode = response.code()
                if (response.isSuccessful) {
                    val responseEntity = response.body()
                    if (responseEntity != null) {
                        callBackInfo.onSuccess(responseEntity)
                    } else handler.post {
                        callBackInfo.onFailure(responseCode, FAILURE_MEG)
                    }
                } else handler.post {
                    callBackInfo.onFailure(responseCode, FAILURE_MEG)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {

            }

        })
    }

    fun login(
        context: Context,
        callBackInfo: CallBackInfo<LoginStatusEntity>,
        loginUserEntity: LoginUserEntity
    ) {
        val loginApi = apiService.login(loginUserEntity)
        loginApi.enqueue(MyCallback<LoginStatusEntity>(callBackInfo, context))
    }

    fun getUserInfoByUsername(
        context: Context,
        callBackInfo: CallBackInfo<UserEntity>,
        username: String
    ) {
        val getUserInfoApi = apiService.getUserInfo(username)
        getUserInfoApi.enqueue(MyCallback(callBackInfo, context))
    }

    class MyCallback<T>(private val callBackInfo: CallBackInfo<T>, private val context: Context) :
        Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val responseCode = response.code()
            if (response.isSuccessful) {
                val responseEntity: T? = response.body()
                if (responseEntity != null) {
                    when (responseEntity) {
                        is VideoEntity -> {
                            callBackInfo.onSuccess(responseEntity)
                        }

                        is UserEntity -> {
                            callBackInfo.onSuccess(responseEntity)
                        }

                        is LoginStatusEntity -> {
                            callBackInfo.onSuccess(responseEntity)
                        }
                    }
                } else handler.post {
                    callBackInfo.onFailure(responseCode, FAILURE_MEG)
                }
            } else handler.post {
                callBackInfo.onFailure(responseCode, FAILURE_MEG)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            handler.post {
                callBackInfo.onFailure(-1, FAILURE_MEG)
            }
        }
    }
}