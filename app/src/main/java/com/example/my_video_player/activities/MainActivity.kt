package com.example.my_video_player.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.my_video_player.R
import com.example.my_video_player.adapters.MainActivityAdapter
import com.example.my_video_player.fragments.HomePageFragment
import com.example.my_video_player.fragments.MinePageFragment
import com.example.my_video_player.fragments.UploadPageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val viewPager = findViewById<ViewPager2>(R.id.view_pager2)
        val fragmentList: MutableList<Fragment> = mutableListOf()
        fragmentList.add(HomePageFragment())
        fragmentList.add(UploadPageFragment())
        fragmentList.add(MinePageFragment())
        val mainActivityAdapter =
            MainActivityAdapter(supportFragmentManager, lifecycle, fragmentList)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = mainActivityAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_page -> viewPager.setCurrentItem(0, false) // 第二个参数为是否平滑滚动
                R.id.upload_page -> viewPager.setCurrentItem(1, false) // 第二个参数为是否平滑滚动
                R.id.mine_page -> viewPager.setCurrentItem(2, false)
            }
            true
        }
    }
    
}