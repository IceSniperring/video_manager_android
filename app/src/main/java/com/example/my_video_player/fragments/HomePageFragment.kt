package com.example.my_video_player.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.my_video_player.R
import com.example.my_video_player.adapters.HomePageAdapter
import com.example.my_video_player.eventsEntities.KindRefreshEvent
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomePageFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var homePageViewPager: ViewPager2
    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private lateinit var homePageAdapter: HomePageAdapter
    private lateinit var emptyArea: ConstraintLayout
    private lateinit var loadingView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)
        homePageViewPager = view.findViewById(R.id.home_page_view_pager2)
        tabLayout = view.findViewById(R.id.tab_layout)
        homePageAdapter = HomePageAdapter(childFragmentManager, lifecycle, fragmentList)
        homePageViewPager.adapter = homePageAdapter
        emptyArea = view.findViewById(R.id.empty_area)
        loadingView = view.findViewById(R.id.loading_view)
        getKindsAndUpdateUI()
        val retryBtn = view.findViewById<TextView>(R.id.retry_text)
        retryBtn.setOnClickListener {
            getKindsAndUpdateUI()
            loadingView.visibility = View.VISIBLE
            emptyArea.visibility = View.GONE
        }
        return view
    }

    private fun getKindsAndUpdateUI() {
        RetrofitUtil.getKind(requireContext(), object : CallBackInfo<List<String>> {
            override fun onSuccess(data: List<String>) {
                loadingView.visibility = View.GONE
                // 清空当前的分类和fragment列表
                fragmentList.clear()
                // 添加 "主页"
                val tabNames = mutableListOf("主页")
                fragmentList.add(RecommendPageFragment())
                data.forEach {
                    tabNames.add(it)
                    fragmentList.add(VideoClassFragment.newInstance(it))
                }

                homePageAdapter.notifyDataSetChanged()

                TabLayoutMediator(tabLayout, homePageViewPager) { tab, position ->
                    if (position < tabNames.size) {
                        tab.text = tabNames[position]
                    }
                }.attach()
            }

            override fun onFailure(code: Int, meg: String) {
                loadingView.visibility = View.GONE
                emptyArea.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "获取数据失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onKindRefreshEvent(event: KindRefreshEvent) {
        getKindsAndUpdateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
