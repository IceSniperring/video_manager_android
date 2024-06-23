package com.example.my_video_player.adapters

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.my_video_player.R
import com.example.my_video_player.activities.HistoryManageActivity
import com.example.my_video_player.activities.PlayPageActivity
import com.example.my_video_player.entities.CommonResponseEntity
import com.example.my_video_player.entities.HistoryVideoItemEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.entities.VideoInfoEntity
import com.example.my_video_player.eventsEntities.HistoryManageListEmptyEvent
import com.example.my_video_player.eventsEntities.KindRefreshEvent
import com.example.my_video_player.eventsEntities.VideoManageListEmptyEvent
import com.example.my_video_player.eventsEntities.VideoRefreshEvent
import com.example.my_video_player.fragments.AlertDialogFragment
import com.example.my_video_player.fragments.NoticeDialogFragment
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus

class HistoryManageAdapter(
    private val historyVideoItemList: MutableList<HistoryVideoItemEntity>,
    private val activity: FragmentActivity
) : BaseQuickAdapter<HistoryVideoItemEntity, BaseViewHolder>(
    R.layout.history_video_item,
    historyVideoItemList
) {
    private val resourceAddress = MMKV.defaultMMKV().decodeString("resourceAddress")
    private val BASE_URL = resourceAddress ?: "http://192.168.31.200:10003"
    override fun convert(holder: BaseViewHolder, item: HistoryVideoItemEntity) {
        val deleteButton = holder.getView<TextView>(R.id.delete_btn)
        deleteButton.setOnClickListener {
            AlertDialogFragment("确定要删除吗？", SpannableString("删除后将无法恢复哦！"), {
                RetrofitUtil.deleteHistory(
                    context,
                    item.recordId,
                    object : CallBackInfo<CommonResponseEntity> {
                        override fun onSuccess(data: CommonResponseEntity) {
                            if (data.success) {
                                historyVideoItemList.remove(item)
                                if (historyVideoItemList.isEmpty()) {
                                    notifyDataSetChanged()
                                } else {
                                    notifyItemRemoved(holder.absoluteAdapterPosition)
                                }
                                if (historyVideoItemList.isEmpty()) {
                                    EventBus.getDefault().postSticky(HistoryManageListEmptyEvent())
                                }
                                NoticeDialogFragment(
                                    "success",
                                    "删除成功",
                                    "删除视频成功"
                                ) {
                                    EventBus.getDefault().postSticky(KindRefreshEvent())
                                    EventBus.getDefault().postSticky(VideoRefreshEvent())
                                }.show(activity.supportFragmentManager, "notice_dialog")
                            } else {
                                NoticeDialogFragment(
                                    //code=1表示删除成功
                                    //code=5表示数据库删除失败，即稿件不存在
                                    //code=6表示未知错误
                                    "error",
                                    "修改结果",
                                    when (data.code) {
                                        5 -> "删除失败！\n数据库删除失败，即稿件不存在"
                                        else -> "删除失败！\n原因：未知错误"
                                    }
                                ) {

                                }.show(
                                    activity.supportFragmentManager,
                                    "notice_dialog"
                                )
                            }
                        }

                        override fun onFailure(code: Int, meg: String) {
                            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                        }
                    })
            }, {}).show(activity.supportFragmentManager, "delete_dialog")
        }
        initView(holder, item)
    }

    @OptIn(UnstableApi::class)
    fun initView(holder: BaseViewHolder, item: HistoryVideoItemEntity) {
        val historyVideoItem = holder.getView<ConstraintLayout>(R.id.history_video_item)
        val poster = holder.getView<ImageFilterView>(R.id.video_poster)
        val title = holder.getView<TextView>(R.id.video_title)
        val date = holder.getView<TextView>(R.id.view_date)
        val author = holder.getView<TextView>(R.id.username)
        author.text = "loading"

        Glide.with(context).load("$BASE_URL${item.postPath}").into(poster)
        title.text = item.title
        date.text = item.viewDate

        //由于后端原因，没有直接给我作者信息，我就用现有接口进行请求
        RetrofitUtil.getVideoByVid(
            item.videoId,
            object : CallBackInfo<List<VideoInfoEntity>> {
                override fun onSuccess(data: List<VideoInfoEntity>) {
                    if (data.isNotEmpty()) {
                        val uid = data[0].uid
                        RetrofitUtil.getUserInfoById(object : CallBackInfo<UserEntity> {
                            override fun onSuccess(data: UserEntity) {
                                author.text = data.username
                                historyVideoItem.setOnClickListener {
                                    val intent = Intent(context, PlayPageActivity::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("filePath", "$BASE_URL${item.filePath}")
                                    bundle.putString("title", item.title)
                                    bundle.putString(
                                        "authorImage",
                                        "$BASE_URL${data.avatarPath}"
                                    )
                                    bundle.putString("authorName", data.username)
                                    bundle.putString("uploadDate", item.viewDate)
                                    bundle.putLong("vid", item.videoId)
                                    intent.putExtras(bundle)
                                    context.startActivity(intent)
                                }
                            }

                            override fun onFailure(code: Int, meg: String) {
                                Log.e("getUserInfoById", "onFailure: $meg")
                            }
                        }, uid)
                    }
                }

                override fun onFailure(code: Int, meg: String) {
                    Log.d("getVideoByVid", "onFailure: $meg")
                }
            })
    }
}