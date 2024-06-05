package com.example.my_video_player.entities

class VideoEntity(
    val records:MutableList<VideoInfoEntity>,
    val total:Int,
    val size:Int,
    val current:Int,
    val pages:Int
) {
}