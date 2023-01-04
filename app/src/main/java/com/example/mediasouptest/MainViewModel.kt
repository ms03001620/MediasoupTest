package com.example.mediasouptest

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediasouptest.media.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mediasoup.droid.Producer
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.model.Peer

class MainViewModel : ViewModel() {
    private val roomClientConfig = RoomClientConfig()
    val peersLiveData = MutableLiveData<List<Peer>>()
    val peersInfoLiveData = MutableLiveData<List<PeerInfo>?>()
    val consumerScoreLiveData = SingleLiveEvent<ConsumerScore>()
    val peersInfoFactory = PeersInfoFactory()
    val joinedLiveData = MutableLiveData<Boolean>()
    var roomClient: RoomClient? = null
    val onConsumerChange = SingleLiveEvent<List<ConsumerHolder>>()
    val onProductSelf = SingleLiveEvent<Producer>()
    var localDeviceHelper: LocalDeviceHelper? = null
    private var mWorkHandler: Handler? = null

    fun asyncTask(runnable: () -> Unit) {
/*        mWorkHandler?.post {
            runnable.invoke()
        }*/
        viewModelScope.launch(Dispatchers.IO) {
            runnable.invoke()
        }
    }

    fun loadConfig(context: Context) {
        roomClientConfig.loadFromShare(context)
        roomClientConfig.loadFixedRoomId()
        roomClientConfig.print()

        // init worker handler.
        val handlerThread = HandlerThread("worker")
        handlerThread.start()
        mWorkHandler = Handler(handlerThread.getLooper())
    }

    fun initSdk(initCallback: org.protoojs.droid.Peer.Listener? = null) {
        roomClient = RoomClient(mWorkHandler, viewModelScope, createOnRoomClientEvent())
        roomClient?.init(roomClientConfig, localDeviceHelper?.createPeerConnectionOptions(), initCallback)
    }

    fun close() {
        // 主线程调用需要直接关闭
        roomClient?.end()
        roomClient = null
        localDeviceHelper?.dispose()
        localDeviceHelper = null
    }

    fun join() {
        //roomClient?.join(createOnRoomClientEvent())
    }


    private fun createOnRoomClientEvent() = object : OnRoomClientEvent {
        override fun onPeersChange(peers: List<Peer>) {
            printThread()
            peersLiveData.postValue(peers)
            peersInfoFactory.syncPeers(peers)
            peersInfoLiveData.postValue(peersInfoFactory.getCopy())
        }

        override fun onConsumersChange(consumers: List<ConsumerHolder>) {
            onConsumerChange.postValue(consumers)
            peersInfoFactory.updateConsumers(consumers)
            peersInfoLiveData.postValue(peersInfoFactory.getCopy())
        }

        override fun onJoin() {
            joinedLiveData.postValue(true)
        }

        override fun onConsumerScore(consumerScore: ConsumerScore) {
            consumerScoreLiveData.postValue(consumerScore)
        }
    }

    fun openCamera() {
        roomClient?.openCamera(localDeviceHelper!!)?.let {
        }
    }

    fun openMic() {
        roomClient?.openMic(localDeviceHelper!!)?.let {
        }
    }

    fun closeMic() {
        roomClient?.closeMic()
    }

    fun closeCamera() {
        roomClient?.closeCamera()
    }

    fun fn() {
        roomClient?.fn()
    }


    fun initLocalDeviceHelper(context: Context) {
        if (localDeviceHelper == null) {
            localDeviceHelper = LocalDeviceHelper(context)
        }
    }



}