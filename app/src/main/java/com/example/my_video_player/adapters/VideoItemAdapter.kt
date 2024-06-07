package com.example.my_video_player.adapters

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.my_video_player.R
import com.example.my_video_player.activities.PlayPageActivity
import com.example.my_video_player.entities.VideoItemEntity

class VideoItemAdapter(private val videoItemList: MutableList<VideoItemEntity>) :
    BaseQuickAdapter<VideoItemEntity, BaseViewHolder>(R.layout.video_item, videoItemList) {
    override fun convert(holder: BaseViewHolder, item: VideoItemEntity) {
        val poster = holder.getView<ImageFilterView>(R.id.poster)
        Glide.with(context).load(item.postPath).into(poster)
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
            bundle.putString("authorImage", "http://192.168.31.200:10003${item.user.avatarPath}")
            bundle.putString("authorName", item.user.username)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }
}