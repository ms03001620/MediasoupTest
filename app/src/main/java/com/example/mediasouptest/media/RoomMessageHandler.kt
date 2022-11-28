package com.example.mediasouptest.media

import org.json.JSONArray
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Logger
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.model.Peer
import org.mediasoup.droid.lib.model.Peers
import org.protoojs.droid.Message
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class RoomMessageHandler(var callback: OnRoomClientEvent? = null) : Consumer.Listener {
    private val mConsumers  = ConcurrentHashMap<String, ConsumerHolder>()
    private val mPeers = CopyOnWriteArrayList<Peer>()

    fun addPeers(data: String?) {
        val json = JsonUtils.toJsonObject(data)
        Logger.d(TAG, "onJoinRoom ${json.toString()}")
        val peersArray = json.optJSONArray("peers") ?: JSONArray()
        Peers.createPeers(peersArray).let {
            addPeers(it)
        }
    }

    //{"id":"rvunszs6","displayName":"Name:honor_red","device":{"flag":"android","name":"Android HWBKL","version":"REL"}}
    private fun addPeer(jsonObject: JSONObject) {
        addPeers(listOf(Peer(jsonObject)))
    }

    private fun addPeers(peer: List<Peer>) {
        Logger.d(TAG, "add Peers: ${peer}")
        mPeers.addAll(peer)
        callback?.onPeersChange(mPeers.toList())
    }

    private fun removePeer(peerId: String) {
        mPeers.find {
            it.id == peerId
        }?.let {
            mPeers.remove(it)
            callback?.onPeersChange(mPeers.toList())
        }
    }

    fun removeClose(consumer: Consumer) {
        assert(consumer.isClosed)
        mConsumers.remove(consumer.id)?.let {
            Logger.d(TAG, "removeClose:${it.peerId}")
            callback?.onConsumersChange(geConsumers())
        }
    }

    private fun removeConsumerAndClose(consumerId: String) {
        mConsumers.remove(consumerId)?.let {
            try {
                it.consumer.close()
                callback?.onConsumersChange(geConsumers())
            } catch (e: Exception) {
                Logger.e(TAG, "removeConsumerAndClose", e)
            }
        }
    }

    fun add(consumerHolder: ConsumerHolder) {
        Logger.d(TAG, "addConsumer: ${consumerHolder.println()}")
        mConsumers[consumerHolder.consumer.id] = consumerHolder
        callback?.onConsumersChange(geConsumers())
    }

    fun geConsumers() = mConsumers.map { it.value }

    fun handleNotification(notification: Message.Notification) {
        if (notification.method == "downlinkBwe" ||
            notification.method == "activeSpeaker"
        ) {
            // Noisy, dismiss
            return
        }
        Logger.d(TAG, "method:${notification.method}, j:${notification.data.toString()}")
        val data = notification.data
        when (notification.method) {
            "producerScore" -> {

                // {"producerId":"bdc2e83e-5294-451e-a986-a29c7d591d73","score":[{"score":10,"ssrc":196184265}]}
                val producerId = data.getString("producerId")
                val score = data.getJSONArray("score")
            }
            "newPeer" -> {
                //method:newPeer, j:{"id":"rvunszs6","displayName":"Name:honor_red","device":{"flag":"android","name":"Android HWBKL","version":"REL"}}
                val id = data.getString("id")
                val displayName = data.optString("displayName")
                addPeer(data)
                //mStore.addPeer(id, data)
                //mStore.addNotify("$displayName has joined the room")
            }
            "peerClosed" -> {
                //{"peerId":"rvunszs6"}
                val peerId = data.getString("peerId")
                removePeer(peerId)
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
                //{"consumerId":"e398df73-fa1c-4073-8ca3-bb0ee99a717d"}
                val consumerId = data.getString("consumerId")

                removeConsumerAndClose(consumerId)

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
                Logger.v(TAG, "unknown protoo notification.method " + notification.method)
            }
        }
    }

    companion object{
        const val TAG = "RoomMessageHandler"
    }

    //Consumer.Listener
    override fun onTransportClose(consumer: Consumer) {
        Logger.d(TAG, "onTransportClose:${consumer.id}")
        assert(false)// TODO Consumer close by native
        removeClose(consumer)
    }

    fun release() {
        callback = null
    }
}