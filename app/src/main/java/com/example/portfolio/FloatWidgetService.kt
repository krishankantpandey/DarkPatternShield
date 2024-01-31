package com.example.portfolio

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi

class FloatWidgetService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingWidget: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RtlHardcoded", "InflateParams")
    override fun onCreate() {
        super.onCreate()
        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.RIGHT
        params.x = 0
        params.y = 100
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        mWindowManager?.addView(mFloatingWidget, params)

        mFloatingWidget?.findViewById<ImageButton>(R.id.btn_widget)?.setOnClickListener {

            val intent1 = Intent(this,MyAccessibilityService::class.java)
//            val intent2 = Intent(this,ScreenCaptureService::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startService(intent1)
//            startService(intent2)

            NotificationUtils.showNotification(this, "Dark Pattern Detected", "Description")

        }
    }




}