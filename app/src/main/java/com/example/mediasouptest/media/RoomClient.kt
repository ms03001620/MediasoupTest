package com.example.mediasouptest.media

import android.content.Context
import android.os.Handler
import org.json.JSONArray
import org.json.JSONObject
import org.mediasoup.droid.Consumer
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

class RoomClient(val workHandler: Handler) {
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
                        val producing = roomClientConfig.roomOptions.isProduce
                        val consuming = roomClientConfig.roomOptions.isConsume
                        val tcp = roomClientConfig.roomOptions.isForceTcp

                        val req = JSONObject()
                        req.put("forceTcp", tcp)
                        req.put("producing", producing)
                        req.put("consuming", consuming)
                        req.put("sctpCapabilities", "")

                        mProtoo?.request("createWebRtcTransport", req, object : ClientRequestHandler {
                            override fun resolve(data: String?) {
                                val info = JSONObject(data)
                                deviceLogic = DeviceLogic(routerRtpCapabilities!!, mProtoo!!)
                                if (producing) deviceLogic?.createSendTransport(info)
                                //if (consuming) deviceLogic?.createRecvTransport(info)
                            }

                            override fun reject(error: Long, errorReason: String?) {
                                assert(false, { Logger.e(TAG, "errorReason$errorReason") })
                            }
                        })
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
                        if (!roomClientConfig.roomOptions.isConsume) {
                            handler.reject(-1, "I do not want to consume")
                        } else {
                            deviceLogic?.onNewConsumer(request, object : Consumer.Listener {
                                override fun onTransportClose(consumer: Consumer) {
                                    Logger.d(TAG, "onTransportClose:${consumer.id}")
                                    roomMessageHandler?.removeClose(consumer)
                                }
                            })?.let { newConsumer ->
                                Logger.d(TAG, "newConsumer:${newConsumer.println()}")
                                roomMessageHandler?.add(newConsumer)
                                handler.accept()
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
                        roomMessageHandler = RoomMessageHandler(onRoomClientEvent)
                        val json = toJsonObject(data)
                        Logger.d(TAG, "onJoinRoom ${json.toString()}")
                        val peersArray = json.optJSONArray("peers") ?: JSONArray()
                        Logger.d(TAG, "peers size ${peersArray.length()}")
                        roomMessageHandler?.addPeers(peersArray)
                    }

                    override fun reject(error: Long, errorReason: String?) {
                        Logger.e(TAG, "join reject:$error, $errorReason")
                    }
                })
            }
        }
    }

    fun end() {
        deviceLogic?.end()
        deviceLogic = null
        roomMessageHandler = null
        mProtoo?.close()
        mProtoo = null
    }

    fun hideSelf() {
        deviceLogic?.destroyVideo()
    }

    fun showSelf(localDeviceHelper: LocalDeviceHelper, mContext: Context) =
        deviceLogic?.createSelfTransport(localDeviceHelper, mContext)

    fun showSelfAudio(localDeviceHelper: LocalDeviceHelper, mContext: Context) =
        deviceLogic?.createSelfAudioTransport(localDeviceHelper, mContext)


    companion object {
        const val TAG = "RoomClient"
    }
}