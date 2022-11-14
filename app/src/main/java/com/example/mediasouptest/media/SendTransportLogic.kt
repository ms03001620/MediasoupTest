package com.example.mediasouptest.media

import android.content.Context
import org.json.JSONObject
import org.mediasoup.droid.*
import org.mediasoup.droid.lib.JsonUtils
import org.mediasoup.droid.lib.LocalDeviceHelper

class SendTransportLogic {
    private var mSendTransport: SendTransport? = null

    fun createSendTransport(
        mediasoupDevice: Device,
        info: JSONObject,
        callback: OnCreateSendTransportEvent
    ): Boolean {
        val id = info.optString("id")
        val iceParameters = info.optString("iceParameters")
        val iceCandidates = info.optString("iceCandidates")
        val dtlsParameters = info.optString("dtlsParameters")
        val sctpParameters = info.optString("sctpParameters")

        mSendTransport = mediasoupDevice.createSendTransport(
            createSendTransportListener(callback),
            id,
            iceParameters,
            iceCandidates,
            dtlsParameters,
            DeviceLogic.mocKSctpParameters)
        return true
    }

    fun createSelfTransport(
        localDeviceHelper: LocalDeviceHelper,
        mContext: Context
    ): Producer? {
        localDeviceHelper.enableCamImpl(mContext)

        return mSendTransport?.produce({ producer: Producer? ->
            Logger.e(TAG, "createSelfTransport(), close")
            if (producer != null) {
                //mStore.removeProducer(producer.getId())
                //mCamProducer = null
            }
        }, localDeviceHelper.getVideoTrack(), null, null, null)
    }

    private fun createSendTransportListener(callback: OnCreateSendTransportEvent) = object : SendTransport.Listener {
        override fun onConnect(transport: Transport, dtlsParameters: String) {
            val req = JSONObject()
            req.put("transportId", transport.getId())
            req.put("dtlsParameters", JsonUtils.toJsonObject(dtlsParameters))
            callback.onConnect(req)
        }

        override fun onConnectionStateChange(
            transport: Transport,
            connectionState: String?
        ) {
            Logger.w(TAG, "onConnectionStateChange:${transport.id}, state:${connectionState}")
        }

        override fun onProduce(
            transport: Transport,
            kind: String?,
            rtpParameters: String?,
            appData: String?
        ): String {
            val req = JSONObject()
            req.put("transportId", transport.id)
            req.put("kind", kind)
            req.put("rtpParameters", JsonUtils.toJsonObject(rtpParameters))
            req.put("appData", appData)
            return callback.onProduce(req)
        }

        override fun onProduceData(
            transport: Transport?,
            sctpStreamParameters: String?,
            label: String?,
            protocol: String?,
            appData: String?
        ): String? {
            Logger.d(
                TAG, "onProduceData() called with: transport = $transport, " +
                        "sctpStreamParameters = $sctpStreamParameters, label = $label, " +
                        "protocol = $protocol, appData = $appData"
            )
            return null
        }
    }

    fun end() {
        mSendTransport?.close()
        mSendTransport?.dispose()
        mSendTransport = null
    }

    companion object{
        const val TAG = "SendTransportLogic"
    }
}