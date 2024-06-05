package com.example.my_video_player.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my_video_player.R

/**
 * A simple [Fragment] subclass.
 * Use the [MinePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MinePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mine_page, container, false)
    }
}