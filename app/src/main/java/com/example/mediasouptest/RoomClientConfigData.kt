package org.mediasoup.droid.demo

data class RoomClientConfigData(
    val roomId: String,
    val peerId: String,
    val displayName: String,
    val forceH264: Boolean,
    val forceVp9: Boolean
)