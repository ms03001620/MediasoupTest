package com.example.mediasouptest

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.media.RoomClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mediasoup.droid.Producer
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.model.Peer

class MainViewModel: ViewModel() {
    private val roomClientConfig = RoomClientConfig()
    val peersLiveData = MutableLiveData<List<Peer>>()
    var roomClient: RoomClient? = null
    val onNewConsumer = SingleLiveEvent<List<ConsumerHolder>>()
    val onProductSelf = SingleLiveEvent<Producer>()
    private var mWorkHandler: Handler? = null

    fun loadConfig(context: Context) {
        roomClientConfig.loadFromShare(context)
        roomClientConfig.loadFixedRoomId()
        roomClientConfig.print()

        // init worker handler.
        var handlerThread = HandlerThread("worker")
        handlerThread.start()
        mWorkHandler = android.os.Handler(handlerThread.getLooper())

        viewModelScope.launch(Dispatchers.IO) {
            localDeviceHelper = LocalDeviceHelper()
            localDeviceHelper?.start()
        }
    }

    fun initSdk() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient = RoomClient(mWorkHandler!!)
            roomClient?.init(roomClientConfig)
            roomClient?.start()
        }
    }

    fun close() {
        viewModelScope.launch(Dispatchers.IO) {
            localDeviceHelper?.dispose()
            localDeviceHelper=null
            roomClient?.end()
        }
    }

    fun join() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient?.join(createOnRoomClientEvent())
        }
    }


    private fun createOnRoomClientEvent() = object : RoomClient.OnRoomClientEvent {
        override fun onPeersChange(peers: List<Peer>) {
            peersLiveData.postValue(peers)
        }

        override fun onVideoConsumersChange(consumers: List<ConsumerHolder>) {
            onNewConsumer.postValue(consumers)
        }
    }

    fun showSelf(applicationContext: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            assert(localDeviceHelper!=null)
            roomClient?.showSelf(localDeviceHelper!!, applicationContext)?.let {
                onProductSelf.postValue(it)
            }

            roomClient?.showSelfAudio(localDeviceHelper!!, applicationContext)
        }

    }

    fun hideSelf(){
        viewModelScope.launch(Dispatchers.IO) {
            roomClient?.hideSelf()
        }
    }

    var localDeviceHelper: LocalDeviceHelper? = null

}