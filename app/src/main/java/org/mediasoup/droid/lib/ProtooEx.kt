package org.mediasoup.droid.lib

import org.json.JSONObject
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
}