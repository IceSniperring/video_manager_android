package com.example.my_video_player.classes

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

class ProgressRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val contentType: MediaType,
    private val progressListener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun onProgress(progress: Int)
    }
    override fun contentType(): MediaType? {
        return contentType
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return
            source = inputStream.source()
            val buffer = Buffer()
            var totalBytesRead: Long = 0
            var bytesRead: Long
            val bufferSize = 8 * 1024 // 8K buffer
            var lastProgress = 0

            while (source.read(buffer, bufferSize.toLong()).also { bytesRead = it } != -1L) {
                sink.write(buffer, bytesRead)
                totalBytesRead += bytesRead
                val progress = (totalBytesRead * 100 / contentLength()).toInt()
                if (progress != lastProgress) {
                    progressListener.onProgress(progress)
                    lastProgress = progress
                }
            }
        } finally {
            source?.close()
        }
    }
}
