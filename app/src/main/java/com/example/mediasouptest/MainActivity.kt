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
    private var currentAudioDevice: AppRTCAudioManager.AudioDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initEvent()
        initConfig()
        initObserver()
        initAudio()
    }

    private fun initAudio() {
        audioManager = AppRTCAudioManager.create(applicationContext, AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        audioManager?.start(AudioManagerEvents { audioDevice, availableAudioDevices ->
            Log.d(
                "MainActivity",
                "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + audioDevice
            )
            currentAudioDevice = audioDevice
            updateAudioInfoStrings()
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
        mainViewModel.joinedLiveData.observe(this) {
            mainViewModel.openCamera()
            mainViewModel.openMic()
            binding.btnOpenCamMic.isEnabled = false

            binding.btnSpeakerOn.isEnabled = true
            binding.btnSpeakerOff.isEnabled = true
            binding.btnBlueOn.isEnabled = true
            binding.btnBlueOff.isEnabled = true
            updateAudioInfoStrings()
        }
    }

    private fun updateAudioInfoStrings(){
        binding.textAudioInfo.text = stringsAudioInfo()
    }

    private fun stringsAudioInfo(): String {
        return "${AudioUtils.AudioManagerInfo(this)}, current:${currentAudioDevice}"
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
            audioManager?.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        }

        binding.btnSpeakerOff.setOnClickListener {
            audioManager?.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
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
        binding.btnOpenCamMic.setOnClickListener {

        }
        binding.btnFn.setOnClickListener {
            updateAudioInfoStrings()
        }
    }
}