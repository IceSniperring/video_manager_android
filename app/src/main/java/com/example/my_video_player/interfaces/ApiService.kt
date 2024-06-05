package com.example.my_video_player.interfaces

import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.UserEntity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/getRandomVideo")
    fun getHomeRandomVideo(@Query("current") current: Int): Call<VideoEntity>

    @GET("api/getUserById")
    fun getUserById(@Query("id") id: Long): Call<UserEntity>

    @GET("api/getKind")
    fun getKind(): Call<List<String>>

    @GET("api/getVideoByKind")
    fun getVideoByKind(
        @Query("kind") kind: String,
        @Query("page") page: Int
    ): Call<VideoEntity>

}