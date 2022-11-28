package com.example.mediasouptest.media

import android.content.Context
import android.os.Looper
import android.preference.PreferenceManager
import org.mediasoup.droid.Logger
import org.mediasoup.droid.PeerConnection
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.AudioTrack
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler
import org.webrtc.VideoTrack

//local
class LocalDeviceHelper(val context: Context) {
    lateinit var peerConnectionUtils: PeerConnectionUtils

    var localVideoTrack: VideoTrack? = null
    var localAudioTrack: AudioTrack? = null

    init {
        peerConnectionUtils = PeerConnectionUtils()


        //val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        //val camera = preferences.getString("camera", "front")
        PeerConnectionUtils.setPreferCameraFace("front"/*camera*/)
    }

    fun createPeerConnectionOptions(): PeerConnection.Options {
        val options = PeerConnection.Options()
        options.setFactory(peerConnectionUtils.getPeerConnectionFactory(context))
        return options;
    }

    fun start() {
    }

    fun switchCam(switchHandler: CameraSwitchHandler) {
        peerConnectionUtils.switchCam(switchHandler)
    }

    fun enableMicImpl() {
        if (localAudioTrack == null) {
            localAudioTrack = peerConnectionUtils.createAudioTrack(context, "mic")
            localAudioTrack?.setEnabled(true)
        }
    }

    fun enableCamImpl() {
        if (localVideoTrack == null) {
            localVideoTrack = peerConnectionUtils.createVideoTrack(context, "cam")
            localVideoTrack?.setEnabled(true)
        }
    }

    fun getAudioTrack(): AudioTrack {
        return localAudioTrack ?: throw IllegalStateException("need call enableMicImpl first")
    }

    fun getVideoTrack(): VideoTrack {
        return localVideoTrack ?: throw IllegalStateException("need call enableCamImpl first")
    }

    fun setLocalVideoTrackEnable(enable: Boolean){
        localVideoTrack?.setEnabled(enable)
    }

    fun setLocalAudioTrackEnable(enable: Boolean){
        localAudioTrack?.setEnabled(enable)
    }

    private fun closeVideo() {
        localVideoTrack?.setEnabled(false)
        localVideoTrack?.dispose()
        localVideoTrack = null
    }

    private fun closeAudio() {
        localAudioTrack?.setEnabled(false)
        localAudioTrack?.dispose()
        localAudioTrack = null
    }

    fun dispose() {
        closeAudio()
        closeVideo()
        Logger.w("LocalDeviceHelper", "onDisconnected ${Thread.currentThread().name}, ${Looper.myLooper()== Looper.myLooper()}")
        peerConnectionUtils.dispose()
    }

    fun disposeVideo() {
        closeVideo()
        peerConnectionUtils.disposeVideo()
    }

    fun disposeAudio(){
        closeAudio()
        peerConnectionUtils.disposeAudio()
    }


}