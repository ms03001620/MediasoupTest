package com.example.mediasouptest.media

import org.mediasoup.droid.Consumer

data class ConsumerHolder(val peerId: String, val consumer: Consumer)

fun ConsumerHolder.println(): String {
    val sb = StringBuilder()
    sb.append("peerId:")
    sb.append(peerId)
    sb.append(",consumer ")
    sb.append("id:")
    sb.append(consumer.id)
    sb.append(",localId:")
    sb.append(consumer.localId)
    sb.append(",kind:")
    sb.append(consumer.kind)
    sb.append(",track:")
    sb.append(consumer.track.javaClass.simpleName)
    sb.append(",isPaused:")
    sb.append(consumer.isPaused)
    sb.append(",producerId:")
    sb.append(consumer.producerId)
    return sb.toString()
}