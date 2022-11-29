package com.example.mediasouptest

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestSufBinding
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.VideoTrack

class TestSurfaceViewRenderer : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityTestSufBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initEvent()
        initConfig()
        initObserver()
    }

    private fun initConfig() {
        mainViewModel.loadConfig(applicationContext)
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test_suf)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //binding.videoRenderer.setEnableHardwareScaler(true /* enabled */)
    }

    private fun initObserver() {
        mainViewModel.peersLiveData.observe(this) {
        }
        mainViewModel.onConsumerChange.observe(this) {
            it.forEach {
                val track = it.consumer.track
                if (track is VideoTrack) {
                    if (currentTrack == null) {
                        Log.d("_____", "this${track.id()}")
                        currentTrack = track
                        showRenderer()
                    }
                }
            }
        }
    }

    var currentTrack: VideoTrack? = null

    override fun onDestroy() {
        if (currentTrack != null) {
            releaseRenderer(true)
        }
        super.onDestroy()
    }

    private fun initEvent() {
        binding.btnStart.setOnClickListener {
            mainViewModel.initLocalDeviceHelper(applicationContext)
            mainViewModel.initSdk()
        }
        binding.btnEnd.setOnClickListener {
            releaseRenderer(true)// can not add again
            mainViewModel.close()
        }
        binding.btnAdd.setOnClickListener {
            showRenderer()
        }
        binding.btnRemove.setOnClickListener {
            releaseRenderer(false)//can add again
        }
    }

    private fun showRenderer() {
        assert(currentTrack != null)

        binding.videoRenderer.init(PeerConnectionUtils.getEglContext(), null)
        currentTrack?.addSink(binding.videoRenderer)
    }

    private fun releaseRenderer(force: Boolean) {
        assert(currentTrack != null)

        currentTrack?.removeSink(binding.videoRenderer)
        binding.videoRenderer.release()

        if (force) {
            currentTrack = null
        }
    }

}