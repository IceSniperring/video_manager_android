package com.example.my_video_player.entities

class UploadResponseEntity(
    val code: Int,
    val video: VideoInfoEntity,
    val success: Boolean,
) {
}