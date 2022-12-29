package com.example.mediasouptest.media

import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import org.mediasoup.droid.*
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class DeviceLogic(
    private val routerRtpCapabilities: String,
    private val protoo: Peer,
    private val workHandler: Handler?,
    private val coroutineScope: CoroutineScope,
    private val options: PeerConnection.Options? = null
) {
    private val device = Device()
    private val sendTransportLogic = SendTransportLogic(protoo, workHandler)
    private val recvTransportLogic = RecvTransportLogic(protoo, workHandler, coroutineScope)

    private val rtpCapabilities: String

    init {
        assert(options != null) // https://github.com/haiyangwu/mediasoup-client-android/pull/42
        device.load(routerRtpCapabilities, options)
        rtpCapabilities = device.rtpCapabilities
    }

    fun closeProducerAudio() {
        sendTransportLogic.closeProducerAudio()
    }

    fun closeProducerVideo() {
        sendTransportLogic.closeProducerVideo()
    }

    fun createProducerVideo(localDeviceHelper: LocalDeviceHelper): Boolean {
        if (device.canProduce("video").not()) {
            throw UnsupportedOperationException("producer video")
        }
        return sendTransportLogic.createProducerVideo(localDeviceHelper)
    }

    fun createProducerAudio(localDeviceHelper: LocalDeviceHelper): Boolean {
        if (device.canProduce("audio").not()) {
            throw UnsupportedOperationException("producer audio")
        }
        return sendTransportLogic.createProducerAudio(localDeviceHelper)
    }

    suspend fun createSendTransport(forceTcp: Boolean) =
        sendTransportLogic.createSendTransport(device, forceTcp)

    suspend fun createRecvTransport(forceTcp: Boolean) =
        recvTransportLogic.createRecvTransport(device, forceTcp)

    fun end() {
        sendTransportLogic.end()
        recvTransportLogic.end()
        device.dispose()
    }

    fun fn() {
        recvTransportLogic.fn()
    }

    fun getRtpCapabilities() = rtpCapabilities

    fun onNewConsumer(request: Message.Request,callback: Consumer.Listener?) =
        recvTransportLogic.onNewConsumer(request, callback)

    companion object{
        const val TAG = "DeviceLogic"

        var mocKSctpParameters = "{\"MIS\":1024,\"OS\":1024,\"maxMessageSize\":2000000,\"numStreams\":2048,\"port\":5000}"
    }

}