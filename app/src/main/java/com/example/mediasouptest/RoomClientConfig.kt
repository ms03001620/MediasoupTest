package org.mediasoup.droid.demo

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import org.mediasoup.droid.lib.RoomOptions
import org.mediasoup.droid.lib.Utils

class RoomClientConfig {
    lateinit var data:  RoomClientConfigData
    lateinit var roomOptions: RoomOptions
    lateinit var preferences: SharedPreferences

    fun loadFromShare(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        generateDefaultValue(preferences)
        loadConfigData(preferences)
        loadRoomOption(preferences)
    }

    private fun generateDefaultValue(preferences: SharedPreferences){
        //RoomOption
        if (preferences.contains(PRODUCE).not()) {
            preferences.edit().putBoolean(PRODUCE, true).apply()
        }
        if (preferences.contains(CONSUME).not()) {
            preferences.edit().putBoolean(CONSUME, true).apply()
        }
        if (preferences.contains(FORCE_TCP).not()) {
            preferences.edit().putBoolean(FORCE_TCP, false).apply()
        }
        // Config
        if (preferences.contains(ROOM_ID).not()) {
            preferences.edit().putString(ROOM_ID, Utils.getRandomString(8)).apply()
        }

        if (preferences.contains(PEER_ID).not()) {
            preferences.edit().putString(PEER_ID, Utils.getRandomString(8)).apply()
        }

        if (preferences.contains(DISPLAY_NAME).not()) {
            preferences.edit().putString(DISPLAY_NAME, "Name:" + Utils.getRandomString(8)).apply()
        }

        if (preferences.contains(FORCE_H_264).not()) {
            preferences.edit().putBoolean(FORCE_H_264, false).apply()
        }

        if (preferences.contains(FORCE_VP_9).not()) {
            preferences.edit().putBoolean(FORCE_VP_9, false).apply()
        }
    }

    fun loadFixedRoomId() {
        if ((preferences.getString(ROOM_ID, "") ?: "") != "c5bwfyow") {
            preferences.edit().putString(ROOM_ID, "c5bwfyow").apply()
        }
    }

    private fun loadRoomOption(preferences: SharedPreferences) {
        roomOptions = RoomOptions()
        roomOptions.isProduce = preferences.getBoolean(PRODUCE, true)
        roomOptions.isConsume = preferences.getBoolean(CONSUME, true)
        roomOptions.isForceTcp = preferences.getBoolean(FORCE_TCP, false)
    }

    private fun loadConfigData(preferences: SharedPreferences) {
        val mRoomId = preferences.getString(ROOM_ID, "")?:""
        val mPeerId = preferences.getString(PEER_ID, "")?:""
        val mDisplayName = preferences.getString(DISPLAY_NAME, "")?:""
        val mForceH264 = preferences.getBoolean(FORCE_H_264, false)
        val mForceVP9 = preferences.getBoolean(FORCE_VP_9, false)

        data = RoomClientConfigData(mRoomId, mPeerId, mDisplayName, mForceH264, mForceVP9)
    }

    fun print() {
        Log.d("RoomClientConfig", "data:$data")
        Log.d("RoomClientConfig", "roomOptions:$roomOptions")
    }

    companion object{
        private const val PRODUCE = "produce"
        private const val CONSUME = "consume"
        private const val FORCE_TCP = "forceTcp"

        private const val ROOM_ID = "roomId"
        private const val PEER_ID = "peerId"
        private const val DISPLAY_NAME = "displayName"
        private const val FORCE_H_264 = "forceH264"
        private const val FORCE_VP_9 = "forceVP9"
    }
}