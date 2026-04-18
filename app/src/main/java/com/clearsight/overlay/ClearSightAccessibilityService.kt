package com.clearsight.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class ClearSightAccessibilityService : AccessibilityService() {

    // Ideally, this list comes from user settings. Hardcoded for testing.
    private val activeApps = listOf("com.amazon.kindle", "org.mozilla.firefox", "com.google.android.apps.docs", "com.songbookpro.songbookpro")

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        this.serviceInfo = info
        CSLog.i("Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString()
        CSLog.focus(packageName)

        val isActiveApp = activeApps.any { packageName?.contains(it) == true }
        
        val overlayIntent = Intent(this, OverlayService::class.java)
        if (isActiveApp) {
            overlayIntent.putExtra("ACTION", "SHOW")
        } else {
            overlayIntent.putExtra("ACTION", "HIDE")
        }
        
        // Robust launch with potential delay if needed
        try {
            startService(overlayIntent)
        } catch (e: Exception) {
            CSLog.lifecycle("Accessibility", "Launch failed, retrying...")
        }
    }

    override fun onInterrupt() {
        CSLog.w("Accessibility Service Interrupted")
    }
}
