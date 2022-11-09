package com.example.mediasouptest.media

import android.util.Log
import org.json.JSONObject
import org.mediasoup.droid.Device
import org.mediasoup.droid.lib.Protoo
import org.mediasoup.droid.lib.RoomOptions
import org.protoojs.droid.Peer
import java.util.concurrent.CountDownLatch

class DeviceLogic(val mProtoo: Protoo, val mOptions: RoomOptions) {
    private val mMediasoupDevice: Device
    private val sendTransportLogic = SendTransportLogic()

    init {
        val countDownLatch = CountDownLatch(1)

        try {
            mProtoo.request("getRouterRtpCapabilities", JSONObject(), object: Peer.ClientRequestHandler {
                override fun resolve(data: String?) {
                    Log.d(TAG, "resolve() called with: data = $data")
                    countDownLatch.countDown()
                }

                override fun reject(error: Long, errorReason: String?) {
                    Log.d(TAG, "reject() called with: error = $error, errorReason = $errorReason")
                    countDownLatch.countDown()
                }
            })
        }catch (e: Exception){
            countDownLatch.countDown()
        }

        countDownLatch.await()



        mMediasoupDevice = Device()
/*        val routerRtpCapabilities = mProtoo.syncRequest("getRouterRtpCapabilities")
        mMediasoupDevice.load(routerRtpCapabilities, null)
        val rtpCapabilities: String = mMediasoupDevice.getRtpCapabilities()*/
    }

    fun createSendTransport() {
        sendTransportLogic.createSendTransport(mMediasoupDevice, mProtoo, mOptions)
    }

    fun end() {
        sendTransportLogic.end()
        mMediasoupDevice.dispose()
    }

    companion object{
        const val TAG = "DeviceLogic"

        var mocKSctpParameters = "{\"MIS\":1024,\"OS\":1024,\"maxMessageSize\":2000000,\"numStreams\":2048,\"port\":5000}"
    }

}