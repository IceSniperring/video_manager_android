package com.example.my_video_player.interfaces

import androidx.media3.common.C.ContentType
import com.example.my_video_player.entities.LoginStatusEntity
import com.example.my_video_player.entities.LoginUserEntity
import com.example.my_video_player.entities.UploadResponseEntity
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.entities.UserEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @POST("api/login")
    fun login(
        @Body loginUserEntity: LoginUserEntity
    ): Call<LoginStatusEntity>

    @GET("api/getUserInfo")
    fun getUserInfo(
        @Query("username") username: String
    ): Call<UserEntity>

    @POST("api/videoUpload")
    fun uploadVideo(@Body requestBody: RequestBody): Call<UploadResponseEntity>
}