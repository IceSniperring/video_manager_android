package com.example.my_video_player.utils

import android.os.Handler
import android.os.Looper
import com.example.my_video_player.interceptors.LoggerInterceptor
import com.example.my_video_player.interfaces.ApiService
import com.example.my_video_player.interfaces.CallBackInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConnectTestUtil {
    fun testConnect(callBackInfo: CallBackInfo<List<String>>, serverAddress: String) {
        val handler = Handler(Looper.getMainLooper())
        val okHttpClient = OkHttpClient.Builder().addInterceptor(LoggerInterceptor()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val getKindApi = apiService.getKind()
        getKindApi.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                val responseCode = response.code()
                if (response.isSuccessful) {
                    val responseEntity = response.body()
                    if (responseEntity != null) {
                        callBackInfo.onSuccess(responseEntity)
                    } else handler.post {
                        callBackInfo.onFailure(responseCode, "服务器返回数据为空")
                    }
                } else handler.post {
                    callBackInfo.onFailure(responseCode, "服务器请求失败")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                callBackInfo.onFailure(-1, "服务器连接失败")
            }
        })
    }
}