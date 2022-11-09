package org.mediasoup.droid.lib

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat.getSystemService
import com.example.mediasouptest.BuildConfig


/**
 *
<!--https://developer.android.com/training/scheduling/wakelock-->
<uses-permission android:name="android.permission.WAKE_LOCK" />
 */
class PowerHelper {
    var wakeLock: PowerManager.WakeLock? = null

    // 您绝不需要在 Activity 中使用唤醒锁定。如上所述，如果您希望屏幕在 Activity 中保持开启状态，请使用 FLAG_KEEP_SCREEN_ON。
    fun acquire(context: Context) {
        wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "${BuildConfig.APPLICATION_ID}::calling"
            ).apply {
                acquire(60 * 60 * 1000L)
            }
        }
    }

    fun release() {
        wakeLock?.release()
    }
}