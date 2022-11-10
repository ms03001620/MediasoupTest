package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Device
import org.mediasoup.droid.lib.RoomOptions
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class DeviceLogic(
    private val mOptions: RoomOptions,
    private val routerRtpCapabilities: String
) {
    private val device = Device()
    private val sendTransportLogic = SendTransportLogic()
    private val recvTransportLogic = RecvTransportLogic()

    private val rtpCapabilities: String

    init {
        device.load(routerRtpCapabilities, null)
        rtpCapabilities = device.rtpCapabilities
    }

    fun createSendTransport(info: JSONObject, callback: OnCreateSendTransportEvent) =
        sendTransportLogic.createSendTransport(device, info, callback)

    fun createRecvTransport(info: JSONObject, callback: OnCreateRecvTransportEvent) =
        recvTransportLogic.createRecvTransport(device, info, callback)

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