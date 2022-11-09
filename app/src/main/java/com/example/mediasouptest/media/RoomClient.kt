package com.example.mediasouptest.media

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import org.mediasoup.droid.demo.RoomClientConfig
import org.mediasoup.droid.lib.LocalDeviceHelper
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.UrlFactory
import org.mediasoup.droid.lib.socket.WebSocketTransport
import org.protoojs.droid.Message
import org.protoojs.droid.Peer

class RoomClient {
    private var mProtooUrl: String = ""
    private lateinit var localDeviceHelper: LocalDeviceHelper
    private var mProtoo: Protoo? = null
    lateinit var roomClientConfig: RoomClientConfig
    private var mWorkHandler: Handler? = null

    // main looper handler.
    private var mMainHandler: Handler? = null

    var deviceLogic: DeviceLogic? = null

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
        localDeviceHelper.start()




        // init worker handler.
        val handlerThread = HandlerThread("worke11r")
        handlerThread.start()
        mWorkHandler = Handler(handlerThread.looper)
        //mMainHandler = Handler(Looper.getMainLooper())
    }

    fun start() {
        mProtooUrl = "wss://v3demo.mediasoup.org:4443/?roomId=c5bwfyow&peerId=mh9soc22"
        val transport = WebSocketTransport(mProtooUrl)
        mProtoo = Protoo(transport, peerListener)
    }

    fun end() {
        deviceLogic?.end()
        mProtoo?.close()
    }

    private val peerListener = object: Peer.Listener {
        override fun onOpen() {
            try {
                assert(mProtoo != null)
                mWorkHandler?.post {
                    val isMain = Looper.getMainLooper() == Looper.myLooper()
                    Log.d(TAG, "is Main:$isMain")

                }

                deviceLogic = DeviceLogic(mProtoo!!, roomClientConfig.roomOptions)






                if (roomClientConfig.roomOptions.isProduce()) {
                   // deviceLogic?.createSendTransport()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFail() {
            Log.e(TAG, "onFail() called")
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
            Log.d(TAG, "onClose() called")
        }
    }
    companion object{
        const val TAG = "RoomClient"
    }
}