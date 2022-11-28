package com.example.mediasouptest

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediasouptest.databinding.ActivityMainBinding
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.webrtc.VideoTrack

class MainActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: PeerAdapter

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.remotePeers.layoutManager = LinearLayoutManager(this)
        adapter = PeerAdapter {

        }

        binding.remotePeers.adapter = adapter
    }

    private fun initObserver() {
        mainViewModel.peersLiveData.observe(this) {
            adapter.setPeers(it)
        }
        mainViewModel.onConsumerChange.observe(this) {
            adapter.onVideoConsumer(it.filter { it.consumer.track is VideoTrack })
        }
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
        binding.btnTestSurfaceViewRenderer.setOnClickListener {
            startActivity(Intent(this, TestSurfaceViewRenderer::class.java))
        }
        binding.btnMe.setOnClickListener {
            startActivity(Intent(this, TestMeActivity::class.java))
        }
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
        }
    }
}