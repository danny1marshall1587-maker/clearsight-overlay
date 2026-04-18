package com.clearsight.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button

class FloatingMenuService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var prefs: PreferencesHelper

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesHelper(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showFloatingButton()
    }

    private fun showFloatingButton() {
        if (floatingView != null) return

        val button = Button(this).apply {
            text = "CS"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            alpha = 0.8f
        }
        floatingView = button

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        val params = WindowManager.LayoutParams(
            150, 150, // Fixed small size
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = prefs.lastYPosition.toInt()
        }

        button.setOnTouchListener(object : View.OnTouchListener {
            private var initialY: Int = 0
            private var initialTouchY: Float = 0f
            private var initialX: Int = 0
            private var initialTouchX: Float = 0f
            private var isClick: Boolean = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (Math.abs(event.rawX - initialTouchX) > 10 || Math.abs(event.rawY - initialTouchY) > 10) {
                            isClick = false
                        }
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        
                        // Notify OverlayService
                        prefs.lastYPosition = params.y.toFloat()
                        prefs.lastXPosition = params.x.toFloat()
                        val updateIntent = Intent("com.clearsight.overlay.UPDATE_POS")
                        sendBroadcast(updateIntent)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            val intent = Intent(this@FloatingMenuService, SettingsActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
        CSLog.d("Floating Button Added")
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
    }
}
