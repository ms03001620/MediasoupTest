package com.example.mediasouptest.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.mediasouptest.R
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.MediaStreamTrack
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class VideoWallpaper(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val renderer: SurfaceViewRenderer
    private val mask: View
    private var videoTrack: VideoTrack?=null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_video, this, true)
        mask = findViewById(R.id.mask)
        renderer = findViewById(R.id.video_renderer)
        renderer.init(PeerConnectionUtils.getEglContext(), null)
    }

    fun hideVideo() {
        renderer.clearImage()
        // 画面会卡住，挡一下表示无效
        mask.visibility = View.VISIBLE
    }

    fun showVideo(track: MediaStreamTrack) {
        with(track as VideoTrack) {
           // renderer.init(PeerConnectionUtils.getEglContext(), null)
            videoTrack = track
            track.addSink(renderer)
            mask.visibility = View.GONE
        }
    }

}