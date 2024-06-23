package com.example.my_video_player.adapters

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.my_video_player.R
import com.example.my_video_player.activities.PlayPageActivity
import com.example.my_video_player.entities.CommonResponseEntity
import com.example.my_video_player.entities.VideoInfoEntity
import com.example.my_video_player.eventsEntities.KindRefreshEvent
import com.example.my_video_player.eventsEntities.VideoManageListEmptyEvent
import com.example.my_video_player.eventsEntities.VideoRefreshEvent
import com.example.my_video_player.fragments.AlertDialogFragment
import com.example.my_video_player.fragments.EditVideoDialogFragment
import com.example.my_video_player.fragments.NoticeDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus

class VideoManagerAdapter(
    private val videoInfoEntityList: MutableList<VideoInfoEntity>,
    private val activity: FragmentActivity
) :
    BaseQuickAdapter<VideoInfoEntity, BaseViewHolder>(
        R.layout.video_manage_item, videoInfoEntityList
    ) {
    private val resourceAddress = MMKV.defaultMMKV().decodeString("resourceAddress")
    private val BASE_URL = resourceAddress ?: "http://192.168.31.200:10003"

    @OptIn(UnstableApi::class)
    override fun convert(holder: BaseViewHolder, item: VideoInfoEntity) {
        val poster = holder.getView<ImageFilterView>(R.id.video_poster)
        val title = holder.getView<TextView>(R.id.video_title)
        val kind = holder.getView<TextView>(R.id.video_kind)
        val date = holder.getView<TextView>(R.id.video_date)

        poster.setOnClickListener {
            val intent = Intent(context, PlayPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("filePath", "$BASE_URL${item.filePath}")
            bundle.putString("title", item.title)
            bundle.putString(
                "authorImage",
                "$BASE_URL${MMKV.defaultMMKV().decodeString("avatar")}"
            )
            bundle.putString("authorName", MMKV.defaultMMKV().decodeString("username"))
            bundle.putString("uploadDate", item.uploadDate)
            bundle.putLong("vid", item.id)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        Glide.with(context).load("$BASE_URL${item.postPath}").into(poster)
        title.text = item.title
        kind.text = item.kind
        date.text = item.uploadDate

        val videoDelete = holder.getView<TextView>(R.id.video_delete)
        videoDelete.setOnClickListener {
            AlertDialogFragment("确定删除吗？", SpannableString("删除后将无法恢复"), {
                RetrofitUtil.deleteVideoById(
                    item.id,
                    context,
                    object : CallBackInfo<CommonResponseEntity> {
                        override fun onSuccess(data: CommonResponseEntity) {
                            videoInfoEntityList.remove(item)
                            if (videoInfoEntityList.isEmpty()) {
                                notifyDataSetChanged()
                            } else {
                                notifyItemRemoved(holder.absoluteAdapterPosition)
                            }
                            if (videoInfoEntityList.isEmpty()) {
                                EventBus.getDefault().postSticky(VideoManageListEmptyEvent())
                            }
                            NoticeDialogFragment(
                                "success",
                                "删除成功",
                                "删除视频成功"
                            ) {
                                EventBus.getDefault().postSticky(KindRefreshEvent())
                                EventBus.getDefault().postSticky(VideoRefreshEvent())
                            }.show(activity.supportFragmentManager, "notice_dialog")
                        }

                        override fun onFailure(code: Int, meg: String) {
                            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                        }
                    })
            }, {}).show(activity.supportFragmentManager, "alert_dialog")
        }

        val videoEditor = holder.getView<TextView>(R.id.video_editor)
        videoEditor.setOnClickListener {
            EditVideoDialogFragment(item.id.toString(), item.title, {}).show(
                activity.supportFragmentManager,
                "edit_dialog"
            )
        }
    }
}