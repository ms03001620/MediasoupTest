package com.example.mediasouptest.media

import org.json.JSONObject
import org.mediasoup.droid.Logger
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.UrlFactory
import org.mediasoup.droid.lib.socket.WebSocketTransport
import org.protoojs.droid.Message
import org.protoojs.droid.Peer
import java.util.concurrent.CountDownLatch

class RoomClient {
    private lateinit var roomClientConfig: RoomClientConfig
    private var mProtooUrl: String = ""
    private var localDeviceHelper: LocalDeviceHelper? = null
    private var mProtoo: Protoo? = null
    private var deviceLogic: DeviceLogic? = null

    fun init(roomClientConfig: RoomClientConfig) {
        this.roomClientConfig = roomClientConfig
        val configData = roomClientConfig.data
        mProtooUrl = UrlFactory.getProtooUrl(
            configData.roomId,
            configData.peerId,
            configData.forceH264,
            configData.forceVp9
        )
        localDeviceHelper = LocalDeviceHelper()
        localDeviceHelper?.start()
    }

    fun start() {
        val transport = WebSocketTransport(mProtooUrl)
        mProtoo = Protoo(transport, peerListener)
    }

    fun end() {
        deviceLogic?.end()
        deviceLogic = null
        mProtoo?.close()
        mProtoo = null
        localDeviceHelper?.dispose()
        localDeviceHelper = null
    }

    private val peerListener = object : Peer.Listener {
        override fun onOpen() {
            requestRouterRtpCapabilities()
        }

        override fun onFail() {
            Logger.e(TAG, "onFail() called")
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            TODO("Not yet implemented")
        }

        override fun onNotification(notification: Message.Notification) {
            TODO("Not yet implemented")
        }

        override fun onDisconnected() {
            TODO("Not yet implemented")
        }

        override fun onClose() {
            Logger.d(TAG, "onClose() called")
        }
    }

    private fun requestRouterRtpCapabilities() {
        Logger.d(TAG, "requestRouterRtpCapabilities")
        mProtoo?.request("getRouterRtpCapabilities", JSONObject(),
            object : Peer.ClientRequestHandler {
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

    companion object {
        const val TAG = "RoomClient"
    }
}