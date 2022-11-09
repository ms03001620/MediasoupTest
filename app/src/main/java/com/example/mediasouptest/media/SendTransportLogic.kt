package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.Device
import org.mediasoup.droid.Logger
import org.mediasoup.droid.SendTransport
import org.mediasoup.droid.Transport
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.RoomOptions
import org.protoojs.droid.Peer
import java.util.concurrent.CountDownLatch

class SendTransportLogic {
    private var mSendTransport: SendTransport? = null

    fun createSendTransport(mMediasoupDevice: Device, mProtoo: Protoo, mOptions: RoomOptions) {
        Logger.d(DeviceLogic.TAG, "createSendTransport()")
        val res = mProtoo.syncRequest("createWebRtcTransport") { req: JSONObject? ->
            JsonUtils.jsonPut(req, "forceTcp", mOptions.isForceTcp())
            JsonUtils.jsonPut(req, "producing", true)
            JsonUtils.jsonPut(req, "consuming", false)
            // TODO: sctpCapabilities
            JsonUtils.jsonPut(req, "sctpCapabilities", "")
        }
        val info = JSONObject(res)
        Logger.d(DeviceLogic.TAG, "device#createSendTransport() $info")
        val id = info.optString("id")
        val iceParameters = info.optString("iceParameters")
        val iceCandidates = info.optString("iceCandidates")
        val dtlsParameters = info.optString("dtlsParameters")
        val sctpParameters = info.optString("sctpParameters")
        mSendTransport = mMediasoupDevice.createSendTransport(
            object: SendTransport.Listener{
                override fun onConnect(transport: Transport, dtlsParameters: String) {
                    val req = JSONObject()
                    req.put( "transportId", transport.getId())
                    req.put( "dtlsParameters", JsonUtils.toJsonObject(dtlsParameters))
                    mProtoo.request("connectWebRtcTransport",req, object: Peer.ClientRequestHandler{
                        override fun resolve(data: String?) {
                            Logger.d(TAG, "createSendTransport onConnect resolve:$data")
                        }
                        override fun reject(error: Long, errorReason: String?) {
                            Logger.e(TAG, "createSendTransport onConnect reject:$error, $errorReason")
                        }
                    })
                }

                override fun onConnectionStateChange(transport: Transport, connectionState: String?) {
                    Logger.d(TAG, "createSendTransport onConnectionStateChange:${transport.id}, state:${connectionState}")
                }

                override fun onProduce(
                    transport: Transport,
                    kind: String?,
                    rtpParameters: String?,
                    appData: String?
                ): String {
                    val req = JSONObject()
                    req.put( "transportId", transport.id)
                    req.put( "kind", kind)
                    req.put( "rtpParameters", JsonUtils.toJsonObject(rtpParameters))
                    req.put( "appData", appData)

                    val countDownLatch = CountDownLatch(1)
                    var response = ""
                    mProtoo.request("produce",req, object: Peer.ClientRequestHandler{
                        override fun resolve(data: String?) {
                            Logger.d(TAG, "createSendTransport onProduce resolve:$data")
                            response = data?:""
                            countDownLatch.countDown()
                        }
                        override fun reject(error: Long, errorReason: String?) {
                            Logger.e(TAG, "createSendTransport onProduce reject:$error, $errorReason")
                            countDownLatch.countDown()
                        }
                    })
                    //TODO async kotlin
                    // runBlocking {  }
                    countDownLatch.await()
                    return JSONObject(response).optString("id")
                }


                override fun onProduceData(
                    transport: Transport?,
                    sctpStreamParameters: String?,
                    label: String?,
                    protocol: String?,
                    appData: String?
                ): String? {
                    Logger.d(TAG, "onProduceData() called with: transport = $transport, " +
                            "sctpStreamParameters = $sctpStreamParameters, label = $label, " +
                            "protocol = $protocol, appData = $appData")
                    return null
                }
            },
            id,
            iceParameters,
            iceCandidates,
            dtlsParameters,
            DeviceLogic.mocKSctpParameters
        )
    }

    fun end() {
        mSendTransport?.close()
        mSendTransport?.dispose()
        mSendTransport = null
    }

    companion object{
        const val TAG = "SendTransportLogic"
    }
}