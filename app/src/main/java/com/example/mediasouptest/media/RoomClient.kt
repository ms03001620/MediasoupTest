package com.example.mediasouptest.media

import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mediasoup.droid.Logger
import org.mediasoup.droid.PeerConnection
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.JsonUtils.toJsonObject
import org.mediasoup.droid.lib.ProtooEx.syncReq
import org.mediasoup.droid.lib.UrlFactory
import org.mediasoup.droid.lib.socket.WebSocketTransport
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class RoomClient(
    val workHandler: Handler?,
    val coroutineScope: CoroutineScope,
    val onRoomClientEvent: OnRoomClientEvent
) {
    private lateinit var roomClientConfig: RoomClientConfig
    private var mProtoo: Peer? = null
    private var deviceLogic: DeviceLogic? = null
    private var roomMessageHandler: RoomMessageHandler? = null
    private var options: PeerConnection.Options? = null

    fun init(roomClientConfig: RoomClientConfig, options: PeerConnection.Options?) {
        this.roomClientConfig = roomClientConfig
        this.options = options
        val transport = WebSocketTransport(
            UrlFactory.getProtooUrl(
                /*roomClientConfig.data.roomId*/"c5bwfyow",
                roomClientConfig.data.peerId,
                roomClientConfig.data.forceH264,
                roomClientConfig.data.forceVp9
            )
        )
        mProtoo = Peer(transport, peerListener)
    }

    private val peerListener = object : Peer.Listener {
        override fun onOpen() {
            coroutineScope.launch {
                val resp = mProtoo?.syncReq("getRouterRtpCapabilities", JSONObject())

                if (resp != null) {
                    val routerRtpCapabilities = resp.toString()
                    deviceLogic = DeviceLogic(routerRtpCapabilities, mProtoo!!, workHandler, options)

                    val producing = roomClientConfig.roomOptions.isProduce
                    val consuming = roomClientConfig.roomOptions.isConsume
                    val tcp = roomClientConfig.roomOptions.isForceTcp

                    if (producing) deviceLogic?.createSendTransport(tcp)
                    if (consuming) deviceLogic?.createRecvTransport(tcp)

                    val reqss = JSONObject()
                    reqss.put("displayName", "Ma");
                    reqss.put("device", roomClientConfig.roomOptions.getDevice().toJSONObject());
                    reqss.put("rtpCapabilities", toJsonObject(deviceLogic?.getRtpCapabilities()));
                    reqss.put("sctpCapabilities", "");

                    val joinResp = mProtoo?.syncReq("join", reqss)
                    if(joinResp!=null){
                        onRoomClientEvent.onJoin()
                        roomMessageHandler = RoomMessageHandler(onRoomClientEvent)
                        roomMessageHandler?.addPeers(joinResp.toString())
                    }
                }
            }
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            try {
                Logger.d(TAG, "onRequest${request.method}")
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

    fun end() {
        printThread()
        deviceLogic?.end()
        deviceLogic = null
        roomMessageHandler?.release()
        roomMessageHandler = null
        mProtoo?.close()
        mProtoo = null
    }

    fun openCamera(localDeviceHelper: LocalDeviceHelper) =
        deviceLogic?.createProducerVideo(localDeviceHelper)

    fun openMic(localDeviceHelper: LocalDeviceHelper) =
        deviceLogic?.createProducerAudio(localDeviceHelper)


    fun closeCamera() = deviceLogic?.closeProducerVideo()
    fun closeMic() = deviceLogic?.closeProducerAudio()

    companion object {
        const val TAG = "RoomClient"
    }
}
