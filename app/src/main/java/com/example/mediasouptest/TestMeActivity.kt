package com.example.mediasouptest

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestMeBinding
import com.example.mediasouptest.widget.VideoWallpaper
import org.mediasoup.droid.Producer
import org.mediasoup.droid.lib.PeerConnectionUtils
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
        mainViewModel.onProductSelf.observe(this){
            showOne(it)
        }
    }

    private fun showOne(producer: Producer) {
        findViewById<VideoWallpaper>(R.id.renderer).showVideo((producer.track as VideoTrack))
    }


    private fun initCamera() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val camera = preferences.getString("camera", "front")
        PeerConnectionUtils.setPreferCameraFace(camera)
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
            initCamera()
            mainViewModel.showSelf(getAct())
        }
    }

    fun getAct() = this


}