package com.example.mediasouptest

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestMeBinding
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.widget.VideoWallpaper
import org.webrtc.VideoTrack

class TestMeActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityTestMeBinding

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test_me)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initObserver() {
        mainViewModel.peersLiveData.observe(this) {
        }
        mainViewModel.onNewConsumer.observe(this){
            showOne(it)
        }
    }

    private fun showOne(consumerHolders: List<ConsumerHolder>) {
        Log.d("----", "----")
        consumerHolders.firstOrNull()?.let {
            findViewById<VideoWallpaper>(R.id.renderer).showVideo((it.consumer.track as VideoTrack))
        }
    }

    private fun initEvent() {
        binding.btnStart.setOnClickListener {
            mainViewModel.initSdk()
        }
        binding.btnEnd.setOnClickListener {
            mainViewModel.close()
        }
        binding.btnJoin.setOnClickListener {
            mainViewModel.join()
        }
        binding.btnFn.setOnClickListener {
            val r  = findViewById<VideoWallpaper>(R.id.renderer)
            r.hideVideo()
        }
    }


}