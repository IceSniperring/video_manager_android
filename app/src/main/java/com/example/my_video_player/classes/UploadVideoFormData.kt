package com.example.my_video_player.classes

import android.net.Uri

class UploadVideoFormData(
    var uid: String = "1",
    var title: String = "未命名",
    var kind: String = "未分类",
    var videoUri: Uri = Uri.EMPTY,
    var postUri: Uri = Uri.EMPTY
) {
}