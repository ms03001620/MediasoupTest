package com.example.mediasouptest

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.media.RoomClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.model.Peer
import java.util.ArrayList

class MainViewModel: ViewModel() {
    private val roomClientConfig = RoomClientConfig()
    val peersLiveData = MutableLiveData<List<Peer>>()
    var roomClient: RoomClient? = null
    val onNewConsumer = SingleLiveEvent<ConsumerHolder>()

    fun loadConfig(context: Context) {
        roomClientConfig.loadFromShare(context)
        roomClientConfig.loadFixedRoomId()
        roomClientConfig.print()
    }

    fun initSdk() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient = RoomClient()
            roomClient?.init(roomClientConfig)
            roomClient?.setRoomClientEvent(createOnRoomClientEvent())
            roomClient?.start()
        }
    }

    private fun createOnRoomClientEvent() = object : RoomClient.OnRoomClientEvent {
        override fun onLoadPeers(peers: ArrayList<Peer>) {
            peersLiveData.postValue(peers)
        }

        override fun onNewConsumer(consumerHolder: ConsumerHolder) {
            onNewConsumer.postValue(consumerHolder)
        }
    }

    fun close() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient?.end()
        }
    }

    fun join() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient?.join()
        }
    }
}