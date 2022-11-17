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
            val old = binding.textMessage.text
            val sb = StringBuilder()
            sb.append(old)
            it.forEach {
                sb.append("\n")
                sb.append(it.toString())
            }
            binding.textMessage.text = sb.toString()
        }
        mainViewModel.onProductSelf.observe(this){
            showOne(it)
        }
    }

    private fun showOne(producer: Producer) {
        // 暂不处理显示问题，显示无法释放
        //findViewById<VideoWallpaper>(R.id.renderer).showVideo((producer.track as VideoTrack))
    }

    override fun onDestroy() {
        mainViewModel.close()
        super.onDestroy()
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
        binding.btnCam.setOnClickListener {
            initCamera()
            mainViewModel.showSelf(getAct())
        }
        binding.btnCamClose.setOnClickListener {
            mainViewModel.hideSelf()
        }
    }

    fun getAct() = this


}