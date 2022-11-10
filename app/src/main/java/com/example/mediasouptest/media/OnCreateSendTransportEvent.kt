package com.example.mediasouptest.media

import org.json.JSONObject

interface OnCreateSendTransportEvent {
    fun onConnect(info: JSONObject)
    fun onProduce(info: JSONObject): String
}