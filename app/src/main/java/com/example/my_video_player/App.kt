package com.example.my_video_player

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {
    companion object {
        var instance: App? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this@App)
    }
}