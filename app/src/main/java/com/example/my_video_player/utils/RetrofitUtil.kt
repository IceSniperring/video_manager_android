package com.example.my_video_player.utils

import com.example.my_video_player.classes.ProgressRequestBody
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.example.my_video_player.entities.LoginStatusEntity
import com.example.my_video_player.entities.LoginUserEntity
import com.example.my_video_player.entities.UploadResponseEntity
import com.example.my_video_player.entities.UploadVideoFormData
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoEntity
import com.example.my_video_player.interceptors.LoggerInterceptor
import com.example.my_video_player.interfaces.ApiService
import com.example.my_video_player.interfaces.CallBackInfo
import com.tencent.mmkv.MMKV
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

object RetrofitUtil {
    private val serverAddress = MMKV.defaultMMKV().decodeString("serverAddress")
    private val BASE_URL = serverAddress ?: "http://192.168.31.200:10001"
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(LoggerInterceptor()).build()
    private var retrofit = Retrofit.Builder()
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
                callBackInfo.onFailure(-1, FAILURE_MEG)
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


    fun uploadVideo(
        uploadVideoFormData: UploadVideoFormData,
        context: Context,
        callBackInfo: CallBackInfo<UploadResponseEntity>,
        progressListener: ProgressRequestBody.ProgressListener
    ) {
        try {
            val videoRequestBody = ProgressRequestBody(
                context,
                uploadVideoFormData.videoUri,
                "multipart/form-data".toMediaType(),
                progressListener
            )

            val postRequestBody = ProgressRequestBody(
                context,
                uploadVideoFormData.postUri,
                "multipart/form-data".toMediaType(),
                progressListener
            )

            val uploadVideoApi = apiService.uploadVideo(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uid", uploadVideoFormData.uid)
                    .addFormDataPart("kind", uploadVideoFormData.kind)
                    .addFormDataPart("title", uploadVideoFormData.title)
                    .addFormDataPart(
                        "videoFile",
                        getFileName(context, uploadVideoFormData.videoUri),
                        videoRequestBody
                    )
                    .addFormDataPart(
                        "postFile",
                        getFileName(context, uploadVideoFormData.postUri),
                        postRequestBody
                    )
                    .build()
            )

            uploadVideoApi.enqueue(MyCallback(callBackInfo, context))
        } catch (e: Exception) {
            Log.e("uploadVideo", "Error uploading video", e)
            handler.post {
                callBackInfo.onFailure(-1, FAILURE_MEG)
            }
        }
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

                        is UploadResponseEntity -> {
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

    private fun getRequestBodyFromUri(context: Context, uri: Uri): RequestBody {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileBytes = inputStream?.readBytes()
        inputStream?.close()
        return fileBytes!!.toRequestBody(
            "multipart/form-data".toMediaTypeOrNull(),
            0,
            fileBytes.size
        )
    }

    //通过Uri获取文件名
    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result!!
    }
}