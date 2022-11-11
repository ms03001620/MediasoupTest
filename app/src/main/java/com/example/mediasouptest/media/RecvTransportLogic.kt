package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class RecvTransportLogic {
    private var recvTransport: RecvTransport? = null

    fun createRecvTransport(device: Device, info: JSONObject, callback: OnCreateRecvTransportEvent): Boolean {
        Logger.d(TAG, "device#createRecvTransport() $info")
        val id: String = info.optString("id")
        val iceParameters: String = info.optString("iceParameters")
        val iceCandidates: String = info.optString("iceCandidates")
        val dtlsParameters: String = info.optString("dtlsParameters")
        val sctpParameters: String = info.optString("sctpParameters")

        recvTransport = device.createRecvTransport(
            createRecvTransportListener(callback),
            id,
            iceParameters,
            iceCandidates,
            dtlsParameters,
            DeviceLogic.mocKSctpParameters
        )
        return true
    }

    private fun createRecvTransportListener(callback: OnCreateRecvTransportEvent)= object: RecvTransport.Listener{
        override fun onConnect(transport: Transport, dtlsParameters: String?) {
            val req = JSONObject()
            req.put("transportId", transport.getId())
            req.put("dtlsParameters", JsonUtils.toJsonObject(dtlsParameters))
            callback.onConnect(req)
        }

        override fun onConnectionStateChange(transport: Transport, connectionState: String?) {
            Logger.w(TAG, "onConnectionStateChange: $connectionState")
        }
    }

    fun end() {
        recvTransport?.close()
        recvTransport?.dispose()
        recvTransport = null
    }

    fun onNewConsumer(
        request: Message.Request,
        callback: Consumer.Listener
    ): ConsumerHolder? {
        if (recvTransport == null || recvTransport?.isClosed == true) {
            Logger.w(TAG, "onNewConsumer: recvTransport null")
            return null
        }

        val data = request.data
        val peerId = data.optString("peerId")
        val producerId = data.optString("producerId")
        val id = data.optString("id")
        val kind = data.optString("kind")
        val rtpParameters = data.optString("rtpParameters")
        val type = data.optString("type")
        val appData = data.optString("appData")
        val producerPaused = data.optBoolean("producerPaused")

        val consumer = recvTransport?.consume(callback, id, producerId, kind, rtpParameters, appData)

        if (consumer != null) {
            return ConsumerHolder(peerId, consumer)
        } else {
            return null
        }
    }

    companion object{
        const val TAG = "RecvTransportLogic"
    }
}