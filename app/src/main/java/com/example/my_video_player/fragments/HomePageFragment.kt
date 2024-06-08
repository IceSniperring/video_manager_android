package com.example.my_video_player.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.my_video_player.R
import com.example.my_video_player.adapters.HomePageAdapter
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RetrofitUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)
        val homePageViewPager = view.findViewById<ViewPager2>(R.id.home_page_view_pager2)
        val fragmentList: MutableList<Fragment> = mutableListOf()
        fragmentList.add(RecommendPageFragment())
        val homePageAdapter =
            HomePageAdapter(childFragmentManager, lifecycle, fragmentList)
        homePageViewPager.adapter = homePageAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        RetrofitUtil.getKind(requireContext(), object : CallBackInfo<List<String>> {
            override fun onSuccess(data: List<String>) {
                // 将 "主页" 字符串与 data 列表中的所有元素合并成一个新的数组
                val tabNames = mutableListOf("主页")
                data.forEach {
                    tabNames.add(it)
                    fragmentList.add(VideoClassFragment.newInstance(it))
                }

                TabLayoutMediator(tabLayout, homePageViewPager) { tab, position ->
                    tab.text = tabNames[position]
                }.attach()
            }


            override fun onFailure(code: Int, meg: String) {

            }
        })

        return view
    }
}