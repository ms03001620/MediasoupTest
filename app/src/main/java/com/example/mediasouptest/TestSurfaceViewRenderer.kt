package com.example.mediasouptest

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestSufBinding
import com.example.mediasouptest.media.ConsumerHolder
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
        binding.videoRenderer.init(PeerConnectionUtils.getEglContext(), null)
        binding.videoRenderer.setEnableHardwareScaler(true /* enabled */)
    }

    private fun initObserver() {
        mainViewModel.peersLiveData.observe(this) {
        }
        mainViewModel.onConsumerChange.observe(this){
            showOne(it.filter { it.consumer.track is VideoTrack })
        }
    }

    var currentTrack: VideoTrack? = null

    private fun showOne(consumerHolders: List<ConsumerHolder>) {
        consumerHolders.firstOrNull()?.let {
            with(it.consumer.track as VideoTrack){
                currentTrack = this
                this.addSink(binding.videoRenderer)
            }
        }
    }

    override fun onDestroy() {
        mainViewModel.close()
        super.onDestroy()
    }

    private fun initEvent() {
        binding.btnStart.setOnClickListener {
            mainViewModel.initLocalDeviceHelper(applicationContext)
            mainViewModel.initSdk()
        }
        binding.btnEnd.setOnClickListener {
            mainViewModel.close()
        }
        binding.btnAdd.setOnClickListener {
            //re add need init
            binding.videoRenderer.init(PeerConnectionUtils.getEglContext(), null)
            currentTrack?.addSink(binding.videoRenderer)
        }
        binding.btnRemove.setOnClickListener {
            currentTrack?.removeSink(binding.videoRenderer)
            binding.videoRenderer.release()
        }
    }


}