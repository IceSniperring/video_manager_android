package com.example.my_video_player.adapters

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.my_video_player.R
import com.example.my_video_player.activities.PlayPageActivity
import com.example.my_video_player.entities.VideoItemEntity
import com.tencent.mmkv.MMKV

class VideoItemAdapter(videoItemList: MutableList<VideoItemEntity>) :
    BaseQuickAdapter<VideoItemEntity, BaseViewHolder>(R.layout.video_item, videoItemList) {
    private val resourceAddress = MMKV.defaultMMKV().decodeString("resourceAddress")
    private val BASE_URL = resourceAddress ?: "http://192.168.31.200:10003"

    @OptIn(UnstableApi::class)
    override fun convert(holder: BaseViewHolder, item: VideoItemEntity) {
        val poster = holder.getView<ImageFilterView>(R.id.poster)
        Glide.with(context).load(item.postPath).placeholder(R.drawable.placeholder).into(poster)
        val title = holder.getView<TextView>(R.id.title)
        title.text = item.title
        val username = holder.getView<TextView>(R.id.author)
        username.text = item.user.username

        val videoItem = holder.getView<ConstraintLayout>(R.id.video_item)
        videoItem.setOnClickListener {
            val intent = Intent(context, PlayPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("filePath", item.filePath)
            bundle.putString("title", item.title)
            bundle.putString("authorImage", "$BASE_URL${item.user.avatarPath}")
            bundle.putString("authorName", item.user.username)
            bundle.putString("uploadDate", item.uploadDate)
            bundle.putLong("vid", item.id)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }
}