package com.example.my_video_player.interfaces

import com.example.my_video_player.entities.HomePageRandomVideoEntity
import com.example.my_video_player.entities.UserEntity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/getRandomVideo")
    fun getHomeRandomVideo(@Query("current") current:Int):Call<HomePageRandomVideoEntity>

    @GET("api/getUserById")
    fun getUserById(@Query("id") id:Long):Call<UserEntity>


}