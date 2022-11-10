package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils

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

    companion object{
        const val TAG = "RecvTransportLogic"
    }
}