package com.example.mediasouptest.media

import android.os.Handler
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.ProtooEx.syncReq
import org.protoojs.droid.Peer.ClientRequestHandler

class SendTransportLogic(
    private val protoo: Protoo,
    private val workHandler: Handler?
) {
    private var mSendTransport: SendTransport? = null
    private var selfProducerVideo: Producer? = null
    private var selfProducerAudio: Producer? = null
    private var localDeviceHelper: LocalDeviceHelper? = null

    suspend fun createSendTransport(
        device: Device,
        forceTcp: Boolean,
    ): Boolean {
        val req = JSONObject()
        req.put("forceTcp", forceTcp)
        req.put("producing", true)
        req.put("consuming", false)
        req.put("sctpCapabilities", "")

        protoo.syncReq("createWebRtcTransport", req)?.let { info ->
            val id = info.optString("id")
            val iceParameters = info.optString("iceParameters")
            val iceCandidates = info.optString("iceCandidates")
            val dtlsParameters = info.optString("dtlsParameters")
            val sctpParameters = info.optString("sctpParameters")

            mSendTransport = device.createSendTransport(
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

    fun createProducerVideo(
        localDeviceHelper: LocalDeviceHelper,
    ): Boolean {
        assert(mSendTransport != null)
        if (selfProducerVideo != null) {
            return false
        }
        this.localDeviceHelper = localDeviceHelper
        this.localDeviceHelper?.enableCamImpl()
        selfProducerVideo = mSendTransport!!.produce(object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false)
            }
        }, localDeviceHelper.getVideoTrack(), null, null, null)
        return true
    }

    fun createProducerAudio(
        localDeviceHelper: LocalDeviceHelper,
    ): Boolean {
        assert(mSendTransport != null)
        if (selfProducerAudio != null) {
            return false
        }
        this.localDeviceHelper = localDeviceHelper
        this.localDeviceHelper?.enableMicImpl()
        selfProducerAudio = mSendTransport!!.produce(object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false, { Logger.e(TAG, "a onTransportClose${producer?.id}") })
            }
        }, localDeviceHelper.localAudioTrack, null, null, null)
        return true
    }

    private val listener = object : SendTransport.Listener {

        override fun onConnect(transport: Transport, dtlsParameters: String) {
            val data = JSONObject()
            Logger.d(TAG, "onConnect ${transport.id}")
            data.put("transportId", transport.id)
            data.put("dtlsParameters", JSONObject(dtlsParameters))

            protoo.request("connectWebRtcTransport", data, object: ClientRequestHandler{
                override fun resolve(data: String?) {
                    Logger.d(TAG, "onConnect $data")
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.e(TAG, "onConnect errorReason$errorReason")
                }
            })
        }

        override fun onProduce(
            transport: Transport,
            kind: String?,
            rtpParameters: String?,
            appData: String?
        ): String = runBlocking {
            Logger.d(TAG, "onProduce ${transport.id}")
            val req = JSONObject()
            req.put("transportId", transport.id)
            req.put("kind", kind)
            req.put("rtpParameters", JSONObject(rtpParameters))
            req.put("appData", appData)
            val resp = protoo.syncReq("produce", req)
            val id = resp?.optString("id", "") ?: ""
            assert(id.isNotEmpty())
            return@runBlocking id
        }

        override fun onConnectionStateChange(
            transport: Transport,
            connectionState: String?
        ) {
            Logger.w(TAG, "onConnectionStateChange:${transport.id}, state:${connectionState}")
            when(connectionState){
                // mSendTransport.close() crash
                "closed" -> {
                    end()
                }
            }
        }

        override fun onProduceData(
            transport: Transport?,
            sctpStreamParameters: String?,
            label: String?,
            protocol: String?,
            appData: String?
        ): String? {
            Logger.d(
                TAG, "onProduceData() called with: transport = $transport, " +
                        "sctpStreamParameters = $sctpStreamParameters, label = $label, " +
                        "protocol = $protocol, appData = $appData"
            )
            return null
        }
    }

    fun end() {
        closeProducerVideo()
        closeProducerAudio()
        mSendTransport?.dispose()
    }

    fun closeProducerVideo() {
        selfProducerVideo?.let {
            closeProducer(it) {
                localDeviceHelper?.disposeVideo()
                selfProducerVideo?.close()
                selfProducerVideo = null
            }
        }
    }

    fun closeProducerAudio() {
        selfProducerAudio?.let {
            closeProducer(it) {
                localDeviceHelper?.disposeAudio()
                selfProducerAudio?.close()
                selfProducerAudio = null
            }
        }
    }

    private fun closeProducer(producer: Producer, callback:()->Unit) {
        val producerId = producer.id
        protoo.request("closeProducer", JsonUtils.toJsonObject("producerId", producerId),
            object : ClientRequestHandler {
                override fun resolve(data: String?) {
                    Logger.d(TAG, "postCloseProducer success $producerId")
                    callback.invoke()
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.w(TAG, "postCloseProducer fail $producerId, $errorReason")
                }
            }
        )
    }

    companion object {
        const val TAG = "SendTransportLogic"
    }
}