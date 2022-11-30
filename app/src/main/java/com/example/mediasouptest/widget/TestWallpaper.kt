package com.example.mediasouptest.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.mediasouptest.R

class TestWallpaper(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        if(!isInEditMode){
            LayoutInflater.from(context).inflate(R.layout.item_test, this, true)
            val mask = findViewById<View>(R.id.text1111)
        }
    }

    override fun onAttachedToWindow() {
        Log.d("TestWallpaper", "onAttachedToWindow")
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        Log.d("TestWallpaper", "onDetachedFromWindow")
        super.onDetachedFromWindow()
    }

}