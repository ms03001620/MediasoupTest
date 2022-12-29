package com.example.mediasouptest

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediasouptest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityMainBinding
    lateinit var peersInfoAdapter: PeersInfoAdapter

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
        binding.remotePeers.itemAnimator = null
        peersInfoAdapter = PeersInfoAdapter {
            Toast.makeText(this, it.peer.id.toString(), Toast.LENGTH_LONG).show()
        }
        //binding.remotePeers.adapter = adapter
        binding.remotePeers.adapter = peersInfoAdapter
    }

    private fun initObserver() {
        mainViewModel.peersInfoLiveData.observe(this) {
            peersInfoAdapter.submitList(it)
        }
        mainViewModel.consumerScoreLiveData.observe(this) {
            peersInfoAdapter.updateConsumerScore(it)
        }
    }

    override fun onDestroy() {
        mainViewModel.close()
        super.onDestroy()
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
            mainViewModel.openCamera()
            mainViewModel.openMic()
        }
        binding.btnFn.setOnClickListener {
            mainViewModel.fn()
        }
    }
}