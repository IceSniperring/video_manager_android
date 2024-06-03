package com.example.my_video_player.interfaces

interface CallBackInfo<T> {
    fun onSuccess(data: T)
    fun onFailure(code: Int, meg: String)
}