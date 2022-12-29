package org.mediasoup.droid.lib

import com.example.mediasouptest.media.DeviceLogic
import org.json.JSONObject
import org.mediasoup.droid.demo.RoomClientConfig
import org.protoojs.droid.Peer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ProtooEx {
    suspend fun Peer.syncReq(method: String, data: JSONObject) = suspendCoroutine<JSONObject?> { continuation ->
        request(method, data, object : Peer.ClientRequestHandler {
            override fun resolve(data: String?) {
                continuation.resume(JSONObject(data))
            }

            override fun reject(error: Long, errorReason: String?) {
                continuation.resume(null)
            }
        })
    }

    suspend fun Peer.syncJoinReq(roomClientConfig: RoomClientConfig, deviceLogic: DeviceLogic?) = suspendCoroutine<JSONObject?> { continuation ->
        val reqss = JSONObject()
        reqss.put("displayName", "Ma");
        reqss.put("device", roomClientConfig.roomOptions.getDevice().toJSONObject());
        reqss.put("rtpCapabilities", JsonUtils.toJsonObject(deviceLogic?.getRtpCapabilities()));
        reqss.put("sctpCapabilities", "");

        request("join", reqss, object : Peer.ClientRequestHandler {
            override fun resolve(data: String?) {
                continuation.resume(JSONObject(data))
            }

            override fun reject(error: Long, errorReason: String?) {
                continuation.resume(null)
            }
        })
    }
}