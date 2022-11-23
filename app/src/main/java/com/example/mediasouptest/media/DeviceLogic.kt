package com.example.mediasouptest.media

import android.content.Context
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Device
import org.mediasoup.droid.Logger
import org.mediasoup.droid.Producer
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.protoojs.droid.Message

class DeviceLogic(
    private val routerRtpCapabilities: String,
    private val protoo: Protoo
) {
    private val device = Device()
    private val sendTransportLogic = SendTransportLogic(protoo)
    private val recvTransportLogic = RecvTransportLogic(protoo)
    private var selfProducerVideo: Producer? = null
    private var selfProducerAudio: Producer? = null

    private val rtpCapabilities: String

    init {
        device.load(routerRtpCapabilities, null)
        rtpCapabilities = device.rtpCapabilities
    }

    fun destroyVideo(): String? {
        return selfProducerVideo?.let { producer ->
            val id = producer.id
            producer.close()
            try {
                val resp = protoo.syncRequest("closeProducer", JsonUtils.toJsonObject("producerId", id))
                Logger.d(TAG, "destroyVideo $resp")
            } catch (e: Exception) {
                Logger.e(TAG, "destroyVideo", e)
            }
            id
        }
    }

    fun createSelfTransport(localDeviceHelper: LocalDeviceHelper, mContext: Context) =
        sendTransportLogic.createSelfTransport(localDeviceHelper, mContext, object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false)
            }
        }).also {
            selfProducerVideo = it
        }

    fun createSelfAudioTransport(localDeviceHelper: LocalDeviceHelper, mContext: Context) =
        sendTransportLogic.createSelfAudioTransport(localDeviceHelper, mContext, object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false)
            }
        }).also {
            selfProducerAudio = it
        }


    fun createSendTransport(forceTcp: Boolean) =
        sendTransportLogic.createSendTransport(device, forceTcp)

    fun createRecvTransport(forceTcp: Boolean) =
        recvTransportLogic.createRecvTransport(device, forceTcp)

    fun end() {
        sendTransportLogic.end()
        recvTransportLogic.end()
        device.dispose()
    }

    fun getRtpCapabilities() = rtpCapabilities

    fun onNewConsumer(request: Message.Request,callback: Consumer.Listener) =
        recvTransportLogic.onNewConsumer(request, callback)

    companion object{
        const val TAG = "DeviceLogic"

        var mocKSctpParameters = "{\"MIS\":1024,\"OS\":1024,\"maxMessageSize\":2000000,\"numStreams\":2048,\"port\":5000}"
    }

}