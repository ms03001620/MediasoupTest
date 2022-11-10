package com.example.mediasouptest

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mediasouptest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initEvent()
        initConfig()
    }

    private fun initConfig() {
        mainViewModel.loadConfig(applicationContext)
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
    }


}