package com.example.mediasouptest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediasouptest.databinding.ActivityMainBinding
import mediasoupclientlibrary.audio.AudioUtils
import org.appspot.apprtc.AppRTCAudioManager
import org.appspot.apprtc.AppRTCAudioManager.AudioManagerEvents
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class MainActivity : AppCompatActivity() {
    private val mainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }
    lateinit var binding: ActivityMainBinding
    lateinit var peersInfoAdapter: PeersInfoAdapter
    private var audioManager: AppRTCAudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initEvent()
        initConfig()
        initObserver()
        initAudio()
    }

    private fun initAudio() {
        audioManager = AppRTCAudioManager.create(applicationContext)
        audioManager?.start(AudioManagerEvents { audioDevice, availableAudioDevices ->
            Log.d(
                "MainActivity",
                "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + audioDevice
            )
        })
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
        mainViewModel.audioCtrlLiveData.observe(this) {
            binding.btnSpeakerOn.isEnabled = true
            binding.btnSpeakerOff.isEnabled = true
            binding.btnBlueOn.isEnabled = true
            binding.btnBlueOff.isEnabled = true
            binding.textAudioInfo.text = AudioUtils.AudioManagerInfo(this)
        }
    }

    override fun onDestroy() {
        mainViewModel.close()
        audioManager?.stop()
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
            mainViewModel.initSdk(object: Peer.Listener{
                override fun onOpen() {
                }

                override fun onFail() {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "onFail", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
                }

                override fun onNotification(notification: Message.Notification) {
                }

                override fun onDisconnected() {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "onDisconnected", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onClose() {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "onClose", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        binding.btnSpeakerOn.setOnClickListener {
            AudioUtils.setSpeakerOn(this, true)
        }

        binding.btnSpeakerOff.setOnClickListener {
            AudioUtils.setSpeakerOn(this, false)
        }

        binding.btnBlueOn.setOnClickListener {
            AudioUtils.setBluetoothOn(this, true)
        }

        binding.btnBlueOff.setOnClickListener {
            AudioUtils.setBluetoothOn(this, false)
        }

        binding.btnEnd.setOnClickListener {
            mainViewModel.close()
        }
        binding.btnJoin.setOnClickListener {
            mainViewModel.openCamera()
            mainViewModel.openMic()
        }
        binding.btnFn.setOnClickListener {
            binding.textAudioInfo.text = AudioUtils.AudioManagerInfo(this)
        }
    }
}