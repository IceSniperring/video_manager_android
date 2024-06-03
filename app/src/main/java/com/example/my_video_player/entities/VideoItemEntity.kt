package com.example.my_video_player.entities

class VideoItemEntity(
    val id: Long,
    val title: String,
    val postPath: String,
    val filePath: String,
    val uploadDate: String,
    val user: UserEntity
) {
}