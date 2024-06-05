package com.example.my_video_player.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.my_video_player.R
import com.example.my_video_player.activities.LoginPageActivity
import com.example.my_video_player.entities.LoginStatusEntity
import com.example.my_video_player.entities.LoginUserEntity
import com.example.my_video_player.entities.UserEntity
import com.example.my_video_player.interfaces.CallBackInfo
import com.example.my_video_player.utils.RSAEncrypt
import com.example.my_video_player.utils.RetrofitUtil

class MinePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mine_page, container, false)
        val avatar = view.findViewById<ImageFilterView>(R.id.avatar)
        avatar.setOnClickListener {
            val intent = Intent(requireContext(), LoginPageActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}