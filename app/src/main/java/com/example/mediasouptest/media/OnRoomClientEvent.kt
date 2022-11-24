package com.example.mediasouptest.media

interface OnRoomClientEvent {
    fun onPeersChange(peers: List<org.mediasoup.droid.lib.model.Peer>)
    fun onConsumersChange(consumers: List<ConsumerHolder>)
    fun onJoin()
}