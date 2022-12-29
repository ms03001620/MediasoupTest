package com.example.mediasouptest.media

import android.os.Handler
import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.ProtooEx.syncReq
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class RecvTransportLogic(
    private val protoo: Peer,
    private val workHandler: Handler?
) {
    private var recvTransport: RecvTransport? = null

    suspend fun createRecvTransport(device: Device, forceTcp: Boolean): Boolean {
        val req = JSONObject()
        req.put("forceTcp", forceTcp)
        req.put("producing", false)
        req.put("consuming", true)

        protoo.syncReq("createWebRtcTransport", req)?.let { info ->
            Logger.d(TAG, "device#createRecvTransport() $info")
            val id: String = info.optString("id")
            val iceParameters: String = info.optString("iceParameters")
            val iceCandidates: String = info.optString("iceCandidates")
            val dtlsParameters: String = info.optString("dtlsParameters")
            val sctpParameters: String = info.optString("sctpParameters")

            recvTransport = device.createRecvTransport(
                /*object: RecvTransport.Listener{
                    override fun onConnect(transport: Transport?, dtlsParameters: String?) {
                    }

                    override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
                    }
                }*/
                listener,
                id,
                iceParameters,
                iceCandidates,
                dtlsParameters,
                DeviceLogic.mocKSctpParameters
            )
            return true
        } ?: run {
            return false
        }
    }

    private val listener = object: RecvTransport.Listener{
        override fun onConnect(transport: Transport, dtlsParameters: String?) {
            Logger.d(TAG, "onConnect: id:${transport.id}")
            val req = JSONObject()
            req.put("transportId", transport.getId())
            req.put("dtlsParameters", JsonUtils.toJsonObject(dtlsParameters))

            protoo.request("connectWebRtcTransport", req, object : Peer.ClientRequestHandler {
                override fun resolve(data: String?) {
                    Logger.d(TAG, "connectWebRtcTransport recv $data")
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.e(TAG, "connectWebRtcTransport recv $error, $errorReason")
                }
            })
        }

        override fun onConnectionStateChange(transport: Transport, connectionState: String?) {
            Logger.w(TAG, "onConnectionStateChange: $connectionState, id:${transport.id}")
        }
    }

    fun end() {
        //recvTransport?.close()
        recvTransport?.dispose()
        recvTransport = null
    }

    fun onNewConsumer(
        request: Message.Request,
        callback: Consumer.Listener?
    ): ConsumerHolder {
        if (recvTransport == null || recvTransport?.isClosed == true) {
            throw IllegalStateException("onNewConsumer recvTransport $recvTransport")
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

        assert(!producerPaused)

        val consumer = recvTransport?.consume(callback, id, producerId, kind, rtpParameters, appData)

        if (consumer != null) {
            return ConsumerHolder(peerId, consumer, id, producerId, kind)
        } else {
            throw IllegalStateException("recvTransport?.consume null")
        }
    }

    companion object{
        const val TAG = "RecvTransportLogic"
    }
}