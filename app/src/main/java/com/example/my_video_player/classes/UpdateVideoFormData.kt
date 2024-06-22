package com.example.my_video_player.classes

import android.net.Uri

class UpdateVideoFormData(
    var id: String = "1",
    var title: String = "未命名",
    var postUri: Uri = Uri.EMPTY
) {
}