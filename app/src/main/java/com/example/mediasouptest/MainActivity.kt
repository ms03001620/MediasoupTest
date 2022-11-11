package com.example.mediasouptest

import android.os.Bundle
import android.view.WindowManager
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
        mainViewModel.onNewConsumer.observe(this){
            adapter.onNewConsumer(it)
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
            adapter.removeFist()
        }
    }


}