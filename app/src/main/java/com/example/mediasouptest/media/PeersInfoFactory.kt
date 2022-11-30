package com.example.mediasouptest.media

import com.example.mediasouptest.PeerInfo
import org.mediasoup.droid.lib.model.Peer

class PeersInfoFactory {
    private val data = ArrayList<PeerInfo>()

    fun syncPeers(peers: List<Peer>) {
        peers.map { newPeer->
            val oldPeer = data.find { it.peer.id == newPeer.id }
            if (oldPeer == null) {
                PeerInfo(newPeer, null)
            } else {
                PeerInfo(newPeer, oldPeer.consumerHolder)
            }
        }.let {
            data.clear()
            data.addAll(it)
        }
    }

    fun updateConsumers(consumers: List<ConsumerHolder>) {
        data.map { info ->
            PeerInfo(info.peer, consumers.firstOrNull { holder ->
                holder.peerId == info.peer.id && holder.isVideo()
            })
        }.let {
            data.clear()
            data.addAll(it)
        }
    }

    fun getCopy() = data.toList()
}