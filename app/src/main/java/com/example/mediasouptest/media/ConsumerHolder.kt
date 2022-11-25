package com.example.mediasouptest.media

import android.os.Looper
import android.util.Log
import org.mediasoup.droid.BuildConfig
import org.mediasoup.droid.Consumer

data class ConsumerHolder(val peerId: String, val consumer: Consumer)

//peerId:goaula1r,consumer id:11d82e9d-e46c-4f3c-b790-dbef276ce59e,localId:0,kind:video,track:VideoTrack,isPaused:false,producerId:309d3e5b-4251-4e82-a7f4-7399221a5106
//peerId:goaula1r,consumer id:efe89a43-d118-4701-a502-9a988ad2e87f,localId:1,kind:audio,track:AudioTrack,isPaused:false,producerId:5f5a1b97-cd35-40b6-8d13-d680a1858402
fun ConsumerHolder.println(): String {
    val sb = StringBuilder()
    sb.append("peerId:")
    sb.append(peerId)
    sb.append(",consumer ")
    sb.append("id:")
    sb.append(consumer.id)
    sb.append(",localId:")
    sb.append(consumer.localId)
    sb.append(",kind:")
    sb.append(consumer.kind)
    sb.append(",track:")
    sb.append(consumer.track.javaClass.simpleName)
    sb.append(",isPaused:")
    sb.append(consumer.isPaused)
    sb.append(",producerId:")
    sb.append(consumer.producerId)
    return sb.toString()
}

fun printThread(
    name: String? = null
) {
    if (BuildConfig.DEBUG) {
        var methodName = name
        if (methodName == null) {
            methodName = Thread.currentThread().stackTrace[4].toString()// 4 is printThread parent method
        }
        val isMain = (Looper.myLooper() == Looper.getMainLooper())
        Log.w("ThreadInfo", "isMain:$isMain, $methodName, ${Thread.currentThread().name}")
    }
}