package com.example.mediasouptest

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestSufBinding
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.widget.VideoWallpaper
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
    }

    private fun initObserver() {
        mainViewModel.peersLiveData.observe(this) {
        }
        mainViewModel.onConsumerChange.observe(this){
            showOne(it.filter { it.consumer.track is VideoTrack })
        }
    }

    private fun showOne(consumerHolders: List<ConsumerHolder>) {
        Log.d("----", "----")
        consumerHolders.firstOrNull()?.let {
            findViewById<VideoWallpaper>(R.id.renderer).showVideo((it.consumer.track as VideoTrack))
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
        binding.btnJoin.setOnClickListener {
            mainViewModel.join()
        }
        binding.btnFn.setOnClickListener {
            mainViewModel.openCamera()
            mainViewModel.openMic()
        }
    }


}