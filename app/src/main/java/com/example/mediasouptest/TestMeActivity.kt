package com.example.mediasouptest

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityTestMeBinding
import com.example.mediasouptest.media.println
import org.mediasoup.droid.Producer
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.VideoTrack

class TestMeActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityTestMeBinding

    val handlerUtils = WorkHandlerUtils()

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
            val sb = StringBuilder()
            it.forEach {
                sb.append("\n")
                sb.append(it.toString())
            }
            printLogs(sb.toString())
        }
        mainViewModel.onProductSelf.observe(this){
            showOne(it)
        }
        mainViewModel.joinedLiveData.observe(this) {
            activeBtns(it)
        }
        mainViewModel.onConsumerChange.observe(this) {
            it.forEach {
                printLogs("\n"+it.println())
            }
        }
    }

    private fun activeBtns(active: Boolean){
        binding.btnJoin.isEnabled = !active
        binding.toggleMic.isEnabled = active
        binding.toggleCamera.isEnabled = active
    }

    private fun printLogs(newLog: String){
        val old = binding.textMessage.text
        val sb = StringBuilder()
        sb.append(old)
        sb.append("\n")
        sb.append(newLog.toString())
        binding.textMessage.text = sb.toString()
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
            mainViewModel.initLocalDeviceHelper(applicationContext)
            mainViewModel.initSdk()
        }
        binding.btnEnd.setOnClickListener {
            activeBtns(false)
            mainViewModel.close()
        }
        binding.btnJoin.setOnClickListener {
            mainViewModel.join()
        }
        binding.toggleCamera.setOnCheckedChangeListener { compoundButton, on ->
            if (on) {
                mainViewModel.openCamera()
            } else {
                mainViewModel.closeCamera()
            }
        }

        binding.toggleMic.setOnCheckedChangeListener { compoundButton, on ->
            if (on) {
                mainViewModel.openMic()
            } else {
                mainViewModel.closeMic()
            }
        }
        binding.toggleTest.setOnCheckedChangeListener { compoundButton, on ->
            handlerUtils.loop(300, {
                binding.toggleCamera.performClick()
                binding.toggleMic.performClick()
            })
        }
    }
}