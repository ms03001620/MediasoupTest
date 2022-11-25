package com.example.mediasouptest.media

import android.content.Context
import android.os.Handler
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
    private val protoo: Protoo,
    private val workHandler: Handler
) {
    private val device = Device()
    private val sendTransportLogic = SendTransportLogic(protoo, workHandler)
    private val recvTransportLogic = RecvTransportLogic(protoo, workHandler)

    private val rtpCapabilities: String

    init {
        device.load(routerRtpCapabilities, null)
        rtpCapabilities = device.rtpCapabilities
    }

    fun closeProducer() {
        sendTransportLogic.closeProducerVideo()
        sendTransportLogic.closeProducerAudio()
    }

    fun createProducerVideo(localDeviceHelper: LocalDeviceHelper, mContext: Context): Producer {
        if (device.canProduce("video").not()) {
            throw UnsupportedOperationException("producer video")
        }
        return sendTransportLogic.createProducerVideo(localDeviceHelper, mContext, object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false)
            }
        })
    }

    fun createProducerAudio(localDeviceHelper: LocalDeviceHelper, mContext: Context): Producer {
        if (device.canProduce("audio").not()) {
            throw UnsupportedOperationException("producer audio")
        }
        return sendTransportLogic.createProducerAudio(localDeviceHelper, mContext, object : Producer.Listener {
            override fun onTransportClose(producer: Producer?) {
                assert(false)
            }
        })
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

    fun onNewConsumer(request: Message.Request,callback: Consumer.Listener?) =
        recvTransportLogic.onNewConsumer(request, callback)

    companion object{
        const val TAG = "DeviceLogic"

        var mocKSctpParameters = "{\"MIS\":1024,\"OS\":1024,\"maxMessageSize\":2000000,\"numStreams\":2048,\"port\":5000}"
    }

}