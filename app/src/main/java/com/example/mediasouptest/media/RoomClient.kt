package com.example.mediasouptest.media

import android.content.Context
import android.os.Handler
import org.json.JSONObject
import org.mediasoup.droid.Logger
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.JsonUtils.toJsonObject
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.UrlFactory
import org.mediasoup.droid.lib.socket.WebSocketTransport
import org.protoojs.droid.Message
import org.protoojs.droid.Peer
import org.protoojs.droid.Peer.ClientRequestHandler

class RoomClient(val workHandler: Handler, val callback: () -> Unit) {
    private lateinit var roomClientConfig: RoomClientConfig
    private var mProtoo: Protoo? = null
    private var deviceLogic: DeviceLogic? = null
    private var roomMessageHandler: RoomMessageHandler? = null

    fun init(roomClientConfig: RoomClientConfig) {
        this.roomClientConfig = roomClientConfig
        val transport = WebSocketTransport(
            UrlFactory.getProtooUrl(
                /*roomClientConfig.data.roomId*/"c5bwfyow",
                roomClientConfig.data.peerId,
                roomClientConfig.data.forceH264,
                roomClientConfig.data.forceVp9
            )
        )
        mProtoo = Protoo(transport, peerListener)
    }

    private val peerListener = object : Peer.Listener {

        override fun onOpen() {
            mProtoo?.request("getRouterRtpCapabilities", JSONObject(),
                object : ClientRequestHandler {
                    override fun resolve(routerRtpCapabilities: String?) {
                        deviceLogic = DeviceLogic(routerRtpCapabilities!!, mProtoo!!, workHandler)

                        val producing = roomClientConfig.roomOptions.isProduce
                        val consuming = roomClientConfig.roomOptions.isConsume
                        val tcp = roomClientConfig.roomOptions.isForceTcp

                        if (producing) deviceLogic?.createSendTransport(tcp)
                        if (consuming) deviceLogic?.createRecvTransport(tcp)

                        callback.invoke()// simply to join() logic
                    }

                    override fun reject(error: Long, errorReason: String?) {
                        assert(false, { Logger.e(TAG, "errorReason$errorReason") })
                    }
                })
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            try {
                when (request.method) {
                    "newConsumer" -> {
                        deviceLogic?.onNewConsumer(request, roomMessageHandler)?.let {
                            roomMessageHandler?.add(it)
                            handler.accept()
                        }
                    }
                    else -> {
                        handler.reject(-1, "unsupported:${request.method}")
                    }
                }
            } catch (e: Exception) {
                handler.reject(-1, "error: msg:${e.message} " + request.method)
            }
        }

        override fun onNotification(notification: Message.Notification) {
            try {
                roomMessageHandler?.handleNotification(notification)
            } catch (e: Exception) {
                Logger.e(TAG, "onNotification", e)
            }
        }

        override fun onFail() {
            assert(false, { Logger.e(TAG, "onFail") })
        }

        override fun onDisconnected() {
            assert(false, { Logger.e(TAG, "onDisconnected") })
        }

        override fun onClose() {
            Logger.d(TAG, "onClose() called")
        }
    }

    fun join(onRoomClientEvent: OnRoomClientEvent) {
        assert(deviceLogic != null)
        assert(mProtoo != null)
        mProtoo?.let { protoo ->
            JSONObject().apply {
                this.put("displayName", "Ma");
                this.put("device", roomClientConfig.roomOptions.getDevice().toJSONObject());
                this.put("rtpCapabilities", toJsonObject(deviceLogic?.getRtpCapabilities()));
                this.put("sctpCapabilities", "");
            }.let { json ->
                protoo.request("join", json, object : ClientRequestHandler {
                    override fun resolve(data: String?) {
                        onRoomClientEvent.onJoin()
                        roomMessageHandler = RoomMessageHandler(onRoomClientEvent)
                        roomMessageHandler?.addPeers(data)
                    }

                    override fun reject(error: Long, errorReason: String?) {
                        assert(false, { Logger.e(TAG, "join") })
                    }
                })
            }
        }
    }

    fun end() {
        printThread()
        deviceLogic?.end()
        deviceLogic = null
        roomMessageHandler = null
        mProtoo?.close()
        mProtoo = null
    }

    fun openCamera(localDeviceHelper: LocalDeviceHelper, context: Context) =
        deviceLogic?.createProducerVideo(context, localDeviceHelper)

    fun openMic(localDeviceHelper: LocalDeviceHelper, context: Context) =
        deviceLogic?.createProducerAudio(context, localDeviceHelper)


    fun closeCamera() = deviceLogic?.closeProducerVideo()
    fun closeMic() = deviceLogic?.closeProducerAudio()


    companion object {
        const val TAG = "RoomClient"
    }
}