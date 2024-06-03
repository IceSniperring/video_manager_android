package com.example.my_video_player.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class LoggerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d("request信息：", request.toString())
        val response = chain.proceed(request)
        Log.d("response code：", response.code.toString())
        val responseBody = response.body
        if (responseBody != null) {
            val bodyStr = responseBody.string()
            Log.d("response body：", bodyStr)
            return response.newBuilder()
                .body(ResponseBody.create(responseBody.contentType(), bodyStr))
                .build()
        }
        return response
    }
}