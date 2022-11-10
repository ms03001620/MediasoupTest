package com.example.mediasouptest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediasouptest.media.RoomClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mediasoup.droid.demo.RoomClientConfig

class MainViewModel: ViewModel() {
    private val roomClientConfig = RoomClientConfig()

    var roomClient: RoomClient? = null

    fun loadConfig(context: Context) {
        roomClientConfig.loadFromShare(context)
        roomClientConfig.print()
    }

    fun initSdk() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient = RoomClient()
            roomClient?.init(roomClientConfig)
            roomClient?.start()
        }
    }

    fun close() {
        viewModelScope.launch(Dispatchers.IO) {
            roomClient?.end()
        }

    }
}