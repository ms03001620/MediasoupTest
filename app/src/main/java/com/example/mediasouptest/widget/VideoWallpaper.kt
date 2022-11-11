package com.example.mediasouptest.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.mediasouptest.R
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.SurfaceViewRenderer

class VideoWallpaper(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val renderer: SurfaceViewRenderer

    init {
        LayoutInflater.from(context).inflate(R.layout.item_video, this, true)
        renderer = findViewById(R.id.video_renderer)
        renderer.init(PeerConnectionUtils.getEglContext(), null)
    }

    fun getRenderer() = renderer

}