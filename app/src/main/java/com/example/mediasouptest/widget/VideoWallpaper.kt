package com.example.mediasouptest.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
    private var videoTrack: VideoTrack? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_video, this, true)
        mask = findViewById(R.id.mask)
        renderer = findViewById(R.id.video_renderer)
    }

    fun hideVideo() {
        Log.d("VideoWallpaper", "hideVideo")
        if (videoTrack != null) {
            Log.d("VideoWallpaper", "hideVideo ${videoTrack?.id()}")
            videoTrack?.removeSink(renderer)
            renderer.release()
            videoTrack = null
        }
        mask.visibility = View.VISIBLE
    }

    fun showVideo(track: MediaStreamTrack) {
        Log.d("VideoWallpaper", "showVideo ${track.id()}, cur:${videoTrack?.id()}")
        if (track is VideoTrack && videoTrack == null) {
            videoTrack = track
            renderer.init(PeerConnectionUtils.getEglContext(), null)
            track.addSink(renderer)
            mask.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        Log.d("VideoWallpaper", "onAttachedToWindow")
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        Log.d("VideoWallpaper", "onDetachedFromWindow")
        //videoTrack?.removeSink(renderer)
        //renderer.release()
        super.onDetachedFromWindow()
    }

}