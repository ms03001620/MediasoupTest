package com.example.mediasouptest.media

import android.content.Context
import android.os.Handler
import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.protoojs.droid.Peer.ClientRequestHandler

class SendTransportLogic(
    private val protoo: Protoo,
    private val workHandler: Handler
) {
    private var mSendTransport: SendTransport? = null
    private var selfProducerVideo: Producer? = null
    private var selfProducerAudio: Producer? = null
    private var localDeviceHelper: LocalDeviceHelper? = null

    fun createSendTransport(
        device: Device,
        forceTcp: Boolean,
    ): Boolean {
        val req = JSONObject()
        req.put("forceTcp", forceTcp)
        req.put("producing", true)
        req.put("consuming", false)
        req.put("sctpCapabilities", "")

        protoo.request("createWebRtcTransport", req, object : ClientRequestHandler {
            override fun resolve(data: String?) {
                val info = JSONObject(data)

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
            }

            override fun reject(error: Long, errorReason: String?) {
                assert(false, { Logger.e(TAG, "errorReason$errorReason") })
            }
        })
        return true
    }

    fun createProducerVideo(
        context: Context,
        localDeviceHelper: LocalDeviceHelper,
        autoCloseListener: Producer.Listener,
    ): Producer {
        assert(mSendTransport != null)
        this.localDeviceHelper = localDeviceHelper
        this.localDeviceHelper?.enableCamImpl(context)
        return mSendTransport!!.produce(autoCloseListener, localDeviceHelper.getVideoTrack(), null, null, null).also {
            selfProducerVideo = it
        }
    }

    fun createProducerAudio(
        context: Context,
        localDeviceHelper: LocalDeviceHelper,
        autoCloseListener: Producer.Listener,
    ): Producer {
        assert(mSendTransport != null)
        this.localDeviceHelper = localDeviceHelper
        this.localDeviceHelper?.enableMicImpl(context)
        return mSendTransport!!.produce(autoCloseListener, localDeviceHelper.localAudioTrack, null, null, null).also {
            selfProducerAudio = it
        }
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
        ): String {
            Logger.d(TAG, "onProduce ${transport.id}")
            val req = JSONObject()
            req.put("transportId", transport.id)
            req.put("kind", kind)
            req.put("rtpParameters", JSONObject(rtpParameters))
            req.put("appData", appData)

            try {
                val response = protoo.syncRequest("produce", req)
                val id = JSONObject(response).optString("id")
                return id
            } catch (e: Exception) {
                Logger.e(TAG, "onProduce ${transport.id}", e)
            }
            return ""
        }

        override fun onConnectionStateChange(
            transport: Transport,
            connectionState: String?
        ) {
            Logger.w(TAG, "onConnectionStateChange:${transport.id}, state:${connectionState}")
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
        localDeviceHelper?.disposeVideo()
        selfProducerVideo?.let {
            closeProducer(it)
        }
    }

    fun closeProducerAudio() {
        localDeviceHelper?.disposeAudio()
        selfProducerAudio?.let {
            closeProducer(it)
        }
    }

    private fun closeProducer(producer: Producer) {
        val producerId = producer.id
        producer.close()
        protoo.request("closeProducer", JsonUtils.toJsonObject("producerId", producerId),
            object : ClientRequestHandler {
                override fun resolve(data: String?) {
                    Logger.d(TAG, "postCloseProducer success $producerId")
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.d(TAG, "postCloseProducer fail $producerId, $errorReason")
                }
            }
        )
    }

    companion object {
        const val TAG = "SendTransportLogic"
    }
}