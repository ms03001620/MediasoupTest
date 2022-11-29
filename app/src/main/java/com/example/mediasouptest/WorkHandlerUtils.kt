package com.example.mediasouptest

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.Thread.sleep
import java.util.concurrent.Executors

class WorkHandlerUtils {
    val mWorkHandler: Handler

    val pool = Executors.newCachedThreadPool()

    init {
        mWorkHandler = Handler(Looper.getMainLooper())
        Executors.newCachedThreadPool()
    }

    fun post(callback: () -> Unit, delay: Long) {
        mWorkHandler.postDelayed(callback, delay)
    }

    fun loop(count: Int, callback: () -> Unit, delay: Long = 1000L) {
        pool.execute(Runnable {
            for (i in 0 until count) {
                Log.d("WorkHandlerUtils", "loop$i in：$count")
                sleep(delay)
                post(callback, delay)
            }
        })
    }

    fun quit() {
        mWorkHandler.removeCallbacksAndMessages(null)
    }
}