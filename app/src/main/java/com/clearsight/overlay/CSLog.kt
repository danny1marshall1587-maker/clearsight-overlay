package com.clearsight.overlay

import android.util.Log

/**
 * Forensic Logging Utility for ClearSight Overlay.
 * Designed to track window focus, touch events, and rendering cycles across Universal Android builds.
 */
object CSLog {
    private const val TAG = "CLEARSIGHT_FORENSIC"

    fun d(msg: String) {
        Log.d(TAG, "[DEBUG] $msg")
    }

    fun i(msg: String) {
        Log.i(TAG, "[INFO] $msg")
    }

    fun w(msg: String) {
        Log.w(TAG, "[WARNING] $msg")
    }

    fun e(msg: String, throwable: Throwable? = null) {
        Log.e(TAG, "[ERROR] $msg", throwable)
    }

    fun lifecycle(activity: String, state: String) {
        Log.d(TAG, "[LIFECYCLE] $activity -> $state")
    }

    fun focus(packageName: String?) {
        Log.d(TAG, "[FOCUS_SHIFT] Target App: ${packageName ?: "Unknown"}")
    }
}
