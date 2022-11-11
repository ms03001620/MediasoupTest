package com.example.mediasouptest.media

import org.mediasoup.droid.Logger
import org.protoojs.droid.Message
import org.webrtc.VideoTrack
import java.util.concurrent.ConcurrentHashMap

class RoomMessageHandler {
    private val mConsumers  = ConcurrentHashMap<String, ConsumerHolder>()

    fun remove(id: String) = mConsumers.remove(id)

    fun add(consumerHolder: ConsumerHolder) {
        mConsumers[consumerHolder.consumer.id] = consumerHolder
    }

    fun getVideoConsumers() = mConsumers.toMap().filter {
        it.value.consumer.track is VideoTrack
    }.map {
        it.value
    }

    fun handleNotification(notification: Message.Notification) {
        if (notification.method == "downlinkBwe" ||
            notification.method == "activeSpeaker"
        ) {
            // Noisy, dismiss
            return
        }
        Logger.d("RoomMessageHandler", "method:${notification.method}, j:${notification.data.toString()}")
        val data = notification.data
        when (notification.method) {
            "producerScore" -> {

                // {"producerId":"bdc2e83e-5294-451e-a986-a29c7d591d73","score":[{"score":10,"ssrc":196184265}]}
                val producerId = data.getString("producerId")
                val score = data.getJSONArray("score")
            }
            "newPeer" -> {
                val id = data.getString("id")
                val displayName = data.optString("displayName")
                //mStore.addPeer(id, data)
                //mStore.addNotify("$displayName has joined the room")
            }
            "peerClosed" -> {
                val peerId = data.getString("peerId")
                //mStore.removePeer(peerId)
            }
            "peerDisplayNameChanged" -> {
                val peerId = data.getString("peerId")
                val displayName = data.optString("displayName")
                val oldDisplayName = data.optString("oldDisplayName")
                //mStore.setPeerDisplayName(peerId, displayName)
                //mStore.addNotify("$oldDisplayName is now $displayName")
            }
            "consumerClosed" -> {
                val consumerId = data.getString("consumerId")

                mConsumers.remove(consumerId)?.let {
                    it.consumer.close()
                    //mStore.removeConsumer(holder.peerId, holder.mConsumer.id)
                }
            }
            "consumerPaused" -> {
                val consumerId = data.getString("consumerId")
                mConsumers[consumerId]?.let {
                    //mStore.setConsumerPaused(holder.mConsumer.id, "remote")
                }
            }
            "consumerResumed" -> {
                val consumerId = data.getString("consumerId")
                mConsumers[consumerId]?.let {
                    //mStore.setConsumerResumed(holder.mConsumer.id, "remote")
                }
            }
            "consumerLayersChanged" -> {
                val consumerId = data.getString("consumerId")
                val spatialLayer = data.optInt("spatialLayer")
                val temporalLayer = data.optInt("temporalLayer")
                mConsumers[consumerId]?.let {
                    //mStore.setConsumerCurrentLayers(consumerId, spatialLayer, temporalLayer)
                }
            }
            "consumerScore" -> {
                val consumerId = data.getString("consumerId")
                val score = data.optJSONArray("score")
                mConsumers[consumerId]?.let {
                    //mStore.setConsumerScore(consumerId, score)
                }
            }
            "dataConsumerClosed" -> {}
            "activeSpeaker" -> {
                val peerId = data.getString("peerId")
                //mStore.setRoomActiveSpeaker(peerId)
            }
            else -> {
                Logger.v("RoomMessageHandler", "unknown protoo notification.method " + notification.method)
            }
        }
    }
}