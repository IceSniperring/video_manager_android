package com.example.my_video_player.utils

import android.util.Patterns

object URLValidUtil {
    fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    fun isValidIpAddress(ipAddress: String): Boolean {
        // IP 地址的正则表达式
        val regex = ("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")
        return ipAddress.matches(regex.toRegex())
    }
}