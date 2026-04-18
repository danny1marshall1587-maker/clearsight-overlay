package com.clearsight.overlay

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: OverlayView? = null
    private lateinit var prefs: PreferencesHelper

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.clearsight.overlay.UPDATE_POS" || 
                intent?.action == "com.clearsight.overlay.UPDATE_SETTINGS") {
                overlayView?.invalidate()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesHelper(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val filter = IntentFilter().apply {
            addAction("com.clearsight.overlay.UPDATE_POS")
            addAction("com.clearsight.overlay.UPDATE_SETTINGS")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(updateReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION") ?: "SHOW"
        
        if (action == "SHOW") {
            showOverlay()
        } else {
            hideOverlay()
        }
        
        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return
        
        overlayView = OverlayView(this, prefs)
        
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
        
        // Ensure floating button is also shown if setting is enabled
        if (prefs.showFloatingButton) {
            startService(Intent(this, FloatingMenuService::class.java))
        }
        
        CSLog.d("Overlay View Added")
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
            CSLog.d("Overlay View Removed")
        }
        stopService(Intent(this, FloatingMenuService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        unregisterReceiver(updateReceiver)
        CSLog.lifecycle("OverlayService", "onDestroy")
    }
}

class OverlayView(context: Context, private val prefs: PreferencesHelper) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Apply opacity to color
        val color = prefs.themeColor
        val alphaInt = (prefs.opacity * 255).toInt()
        val finalColor = (color and 0x00FFFFFF) or (alphaInt shl 24)
        paint.color = finalColor

        val theme = prefs.themeType
        val xPos = prefs.lastXPosition + 75f // Center of 150px button
        val yPos = prefs.lastYPosition + 75f
        
        val h = 200f
        val w = 600f

        when (theme) {
            "tint" -> {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            "ruler" -> {
                canvas.drawRect(0f, yPos - h/2, width.toFloat(), yPos + h/2, paint)
            }
            "ruler_v" -> {
                canvas.drawRect(xPos - h/2, 0f, xPos + h/2, height.toFloat(), paint)
            }
            "slit" -> {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                canvas.drawRect(0f, yPos - h/2, width.toFloat(), yPos + h/2, clearPaint)
            }
            "slit_v" -> {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                canvas.drawRect(xPos - h/2, 0f, xPos + h/2, height.toFloat(), clearPaint)
            }
            "spotlight" -> {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                canvas.drawCircle(xPos, yPos, h, clearPaint)
            }
            "crosshair" -> {
                canvas.drawRect(0f, yPos - 2f, width.toFloat(), yPos + 2f, paint)
                canvas.drawRect(xPos - 2f, 0f, xPos + 2f, height.toFloat(), paint)
            }
            "reading_window" -> {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                canvas.drawRect(xPos - w/2, yPos - h/2, xPos + w/2, yPos + h/2, clearPaint)
            }
            "column" -> {
                canvas.drawRect(xPos - w/2, 0f, xPos + w/2, height.toFloat(), paint)
            }
            "dual_anchor" -> {
                val margin = 300f
                canvas.drawRect(0f, 0f, width.toFloat(), margin, paint)
                canvas.drawRect(0f, height.toFloat() - margin, width.toFloat(), height.toFloat(), paint)
            }
            else -> canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }
}
