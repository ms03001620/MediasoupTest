package com.example.mediasouptest.media

import org.json.JSONObject

interface OnCreateRecvTransportEvent {
    fun onConnect(info: JSONObject)
}