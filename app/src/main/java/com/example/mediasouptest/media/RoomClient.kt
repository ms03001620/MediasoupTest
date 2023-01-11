package com.example.mediasouptest.media

import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mediasoup.droid.Logger
import org.mediasoup.droid.PeerConnection
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.ProtooEx.syncJoinReq
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
    private var initCallback: Peer.Listener? = null

    fun init(
        roomClientConfig: RoomClientConfig,
        options: PeerConnection.Options?,
        initCallback: Peer.Listener? = null
    ) {
        this.roomClientConfig = roomClientConfig
        this.options = options
        this.initCallback = initCallback
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
            initCallback?.onOpen()
            coroutineScope.launch {
                val resp = mProtoo?.syncReq("getRouterRtpCapabilities", JSONObject())

                if (resp != null) {
                    val routerRtpCapabilities = resp.toString()
                    deviceLogic = DeviceLogic(routerRtpCapabilities, mProtoo!!, workHandler, coroutineScope, options)

                    val producing = roomClientConfig.roomOptions.isProduce
                    val consuming = roomClientConfig.roomOptions.isConsume
                    val tcp = roomClientConfig.roomOptions.isForceTcp

                    if (producing) deviceLogic?.createSendTransport(tcp)
                    if (consuming) deviceLogic?.createRecvTransport(tcp)

                    val joinResp = mProtoo?.syncJoinReq(roomClientConfig, deviceLogic)
                    if(joinResp!=null){
                        roomMessageHandler = RoomMessageHandler(onRoomClientEvent)
                        roomMessageHandler?.addPeers(joinResp.toString())
                        onRoomClientEvent.onJoin()
                    }
                }
            }
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            initCallback?.onRequest(request, handler)
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
            initCallback?.onNotification(notification)
            try {
                roomMessageHandler?.handleNotification(notification)
            } catch (e: Exception) {
                Logger.e(TAG, "onNotification", e)
            }
        }

        override fun onFail() {
            initCallback?.onFail()
            Logger.d(TAG, "onFail() called")
        }

        override fun onDisconnected() {
            initCallback?.onDisconnected()
            Logger.d(TAG, "onDisconnected() called")
        }

        override fun onClose() {
            initCallback?.onClose()
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

    fun fn() {
        coroutineScope.launch {
            deviceLogic?.fn()
        }
    }

    companion object {
        const val TAG = "RoomClient"
    }
}
