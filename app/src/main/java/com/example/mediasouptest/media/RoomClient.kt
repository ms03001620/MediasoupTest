package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Logger
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.JsonUtils.toJsonObject
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.UrlFactory
import org.mediasoup.droid.lib.model.Peers
import org.mediasoup.droid.lib.socket.WebSocketTransport
import org.protoojs.droid.Message
import org.protoojs.droid.Peer
import org.protoojs.droid.Peer.ClientRequestHandler
import java.util.ArrayList
import java.util.concurrent.CountDownLatch

class RoomClient {
    private lateinit var roomClientConfig: RoomClientConfig
    private var mProtooUrl: String = ""
    private var localDeviceHelper: LocalDeviceHelper? = null
    private var mProtoo: Protoo? = null
    private var deviceLogic: DeviceLogic? = null
    private var onRoomClientEvent: OnRoomClientEvent? = null

    fun init(roomClientConfig: RoomClientConfig) {
        this.roomClientConfig = roomClientConfig
        val configData = roomClientConfig.data
        mProtooUrl = UrlFactory.getProtooUrl(
            /*configData.roomId*/"c5bwfyow",
            configData.peerId,
            configData.forceH264,
            configData.forceVp9
        )
        localDeviceHelper = LocalDeviceHelper()
        localDeviceHelper?.start()
    }

    fun start() {
        val transport = WebSocketTransport(mProtooUrl)
        mProtoo = Protoo(transport, createPeerListener())
    }

    fun end() {
        deviceLogic?.end()
        deviceLogic = null
        mProtoo?.close()
        mProtoo = null
        localDeviceHelper?.dispose()
        localDeviceHelper = null
    }

    private fun createPeerListener() =  object : Peer.Listener {
        val roomMessageHandler = RoomMessageHandler()

        override fun onOpen() {
            requestRouterRtpCapabilities()
        }

        override fun onFail() {
            Logger.e(TAG, "onFail() called")
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            try {
                when (request.method) {
                    "newConsumer" -> {
                        if (!roomClientConfig.roomOptions.isConsume) {
                            handler.reject(-1, "I do not want to consume")
                        } else {
                            deviceLogic?.onNewConsumer(request, object : Consumer.Listener {
                                override fun onTransportClose(consumer: Consumer) {
                                    roomMessageHandler.remove(consumer.id).let {
                                        Logger.d(TAG, "onTransportClose:${it?.peerId ?: ""}")
                                    }
                                }
                            })?.let {
                                Logger.d(TAG, "newConsumer:${it.println()}")
                                roomMessageHandler.add(it)
                                handler.accept()
                                onRoomClientEvent?.onNewConsumer(it)
                                attemptAudioOnly(it)
                            }
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
                roomMessageHandler.handleNotification(notification)
            } catch (e: Exception) {
                Logger.e(TAG, "onNotification", e)
            }
        }

        override fun onDisconnected() {
            Logger.w(TAG, "onDisconnected")
            end()
        }

        override fun onClose() {
            Logger.d(TAG, "onClose() called")
        }
    }

    private fun attemptAudioOnly(consumerHolder: ConsumerHolder) {
        // local audio only
        if (consumerHolder.consumer.kind == "video" && false /*audioOnly*/) {
            requestPauseConsumer(consumerHolder.consumer)
        }
    }

    private fun requestRouterRtpCapabilities() {
        Logger.d(TAG, "requestRouterRtpCapabilities")
        mProtoo?.request("getRouterRtpCapabilities", JSONObject(),
            object : ClientRequestHandler {
                override fun resolve(routerRtpCapabilities: String?) {
                    Logger.d(TAG, "requestRouterRtpCapabilities resolve() $routerRtpCapabilities")
                    routerRtpCapabilities?.let {
                        deviceLogic = createDevice(routerRtpCapabilities)
                        requestCreateWebRtcTransport()
                    }
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.e(TAG, "requestRouterRtpCapabilities $error, errorReason = $errorReason")
                }
            })
    }

    private fun requestCreateWebRtcTransport() {
        val producing = roomClientConfig.roomOptions.isProduce
        val consuming = roomClientConfig.roomOptions.isConsume
        val tcp = roomClientConfig.roomOptions.isForceTcp

        Logger.d(TAG, "requestCreateWebRtcTransport")
        JSONObject().apply {
            this.put("forceTcp", tcp)
            this.put("producing", producing)
            this.put("consuming", consuming)
            this.put("sctpCapabilities", "")
        }.let {
            mProtoo?.request("createWebRtcTransport", it, object : Peer.ClientRequestHandler {
                override fun resolve(data: String?) {
                    Logger.d(TAG, "requestCreateWebRtcTransport resolve() $data")
                    try {
                        data?.let {
                            JSONObject(it)
                        }?.let {
                            if (producing) createSendTransport(it)
                            if (consuming) createRecvTransport(it)
                        } ?: run {
                            throw NullPointerException("requestCreateWebRtcTransport null")
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "requestCreateWebRtcTransport", e)
                    }
                }

                override fun reject(error: Long, errorReason: String?) {
                    Logger.e(TAG, "createWebRtcTransport reject:$error, $errorReason")
                }
            })
        }
    }

    private fun createSendTransport(info: JSONObject) {
        deviceLogic?.createSendTransport(
            info,
            object : OnCreateSendTransportEvent {
                override fun onConnect(info: JSONObject) {
                    requestConnectWebRtcTransport(info, "send")
                }
                override fun onProduce(info: JSONObject) = requestProduce(info)
            }).let {
            Logger.d(TAG, "createSendTransport $it")
        }
    }

    private fun createRecvTransport(info: JSONObject) {
        deviceLogic?.createRecvTransport(
            info,
            object : OnCreateRecvTransportEvent {
                override fun onConnect(info: JSONObject) {
                    requestConnectWebRtcTransport(info, "recv")
                }
            }).let {
            Logger.d(TAG, "createRecvTransport $it")
        }
    }

    private fun createDevice(routerRtpCapabilities: String): DeviceLogic {
        return DeviceLogic(roomClientConfig.roomOptions, routerRtpCapabilities)
    }

    private fun requestConnectWebRtcTransport(info: JSONObject, tag: String) {
        //TODO Is it(tag) necessary to send it twice
        Logger.d(TAG, "requestConnectWebRtcTransport $tag")
        mProtoo?.request("connectWebRtcTransport", info, object : Peer.ClientRequestHandler {
            override fun resolve(data: String?) {
                Logger.d(TAG, "connectWebRtcTransport $tag resolve:$data")
            }

            override fun reject(error: Long, errorReason: String?) {
                Logger.e(TAG, "connectWebRtcTransport $tag reject:$error, $errorReason")
            }
        })
    }

    private fun requestPauseConsumer(consumer: Consumer) {
        Logger.d(TAG, "requestPauseConsumer ${consumer.getId()}, p:${consumer.isPaused},c:${consumer.isClosed}")
        if (consumer.isPaused || consumer.isClosed) {
            return
        }
        mProtoo?.let {
            JSONObject().apply {
                this.put("consumerId", consumer.id)
            }.let {
                mProtoo?.request("pauseConsumer", it, object: ClientRequestHandler{
                    override fun resolve(data: String?) {
                        consumer.pause()
                    }
                    override fun reject(error: Long, errorReason: String?) {
                        Logger.e(TAG, "requestPauseConsumer reject:$error, $errorReason")
                    }
                })
            }
        }
    }


    private fun requestProduce(info: JSONObject): String {
        Logger.d(TAG, "requestProduce")
        val countDownLatch = CountDownLatch(1)
        var response = ""
        mProtoo?.request("produce", info, object : Peer.ClientRequestHandler {
            override fun resolve(data: String?) {
                Logger.d(TAG, "createSendTransport onProduce resolve:$data")
                response = data ?: ""
                countDownLatch.countDown()
            }

            override fun reject(error: Long, errorReason: String?) {
                Logger.e(TAG, "createSendTransport onProduce reject:$error, $errorReason")
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return JSONObject(response).optString("id")
    }

    fun join() {
        assert(deviceLogic != null)
        assert(mProtoo != null)
        mProtoo?.let {  protoo ->
            JSONObject().apply {
                this.put("displayName", "Ma");
                this.put("device", roomClientConfig.roomOptions.getDevice().toJSONObject());
                this.put("rtpCapabilities", toJsonObject(deviceLogic?.getRtpCapabilities()));
                // TODO sctpCapabilities
                this.put( "sctpCapabilities", "");
            }.let { json ->
                protoo.request("join", json, object: ClientRequestHandler{
                    override fun resolve(data: String?) {
                        onJoinRoom(toJsonObject(data))
                    }

                    override fun reject(error: Long, errorReason: String?) {
                        Logger.e(TAG, "join reject:$error, $errorReason")
                    }
                })
            }
        }
    }

    fun onJoinRoom(json: JSONObject){
        Logger.d(TAG, "onJoinRoom ${json.toString()}")
        val peers = json.optJSONArray("peers")
        Logger.d(TAG, "peers size ${peers.length()}")
        onRoomClientEvent?.onLoadPeers(Peers.createPeers(peers))
    }

    fun setRoomClientEvent(onRoomClientEvent: OnRoomClientEvent) {
        this.onRoomClientEvent = onRoomClientEvent
    }

    public interface OnRoomClientEvent {
        fun onLoadPeers(peers: ArrayList<org.mediasoup.droid.lib.model.Peer>)
        fun onNewConsumer(consumerHolder: ConsumerHolder)
    }

    companion object {
        const val TAG = "RoomClient"
    }
}