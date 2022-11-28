package com.example.mediasouptest

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.media.LocalDeviceHelper
import com.example.mediasouptest.media.OnRoomClientEvent
import com.example.mediasouptest.media.RoomClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mediasoup.droid.Producer
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.model.Peer

class MainViewModel : ViewModel() {
    private val roomClientConfig = RoomClientConfig()
    val peersLiveData = MutableLiveData<List<Peer>>()
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
        var handlerThread = HandlerThread("worker")
        handlerThread.start()
        mWorkHandler = Handler(handlerThread.getLooper())
    }

    fun initSdk() {
        roomClient = RoomClient(mWorkHandler!!, {
            join()
        })
        roomClient?.init(roomClientConfig, localDeviceHelper?.createPeerConnectionOptions())
    }

    fun close() {
        // 主线程调用需要直接关闭
        roomClient?.end()
        roomClient = null
        localDeviceHelper?.dispose()
        localDeviceHelper = null
    }

    fun join() {
        roomClient?.join(createOnRoomClientEvent())
    }


    private fun createOnRoomClientEvent() = object : OnRoomClientEvent {
        override fun onPeersChange(peers: List<Peer>) {
            peersLiveData.postValue(peers)
        }

        override fun onConsumersChange(consumers: List<ConsumerHolder>) {
            onConsumerChange.postValue(consumers)
        }

        override fun onJoin() {
            joinedLiveData.postValue(true)
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

    fun test(c: Context){
        initLocalDeviceHelper(c)
        localDeviceHelper?.enableMicImpl()
        localDeviceHelper?.enableCamImpl()
    }


    fun initLocalDeviceHelper(context: Context) {
        if (localDeviceHelper == null) {
            localDeviceHelper = LocalDeviceHelper(context)
        }
    }



}