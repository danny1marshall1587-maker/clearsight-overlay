package com.clearsight.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ClearSightPrefs", Context.MODE_PRIVATE)

    var themeType: String
        get() = prefs.getString("THEME_TYPE", "tint") ?: "tint"
        set(value) = prefs.edit().putString("THEME_TYPE", value).apply()

    var isOverlayActive: Boolean
        get() = prefs.getBoolean("OVERLAY_ACTIVE", false)
        set(value) = prefs.edit().putBoolean("OVERLAY_ACTIVE", value).apply()

    var themeColor: Int
        get() = prefs.getInt("THEME_COLOR", Color.parseColor("#fdf6e3"))
        set(value) = prefs.edit().putInt("THEME_COLOR", value).apply()

    var secondaryColor: Int
        get() = prefs.getInt("SECONDARY_COLOR", Color.parseColor("#268bd2"))
        set(value) = prefs.edit().putInt("SECONDARY_COLOR", value).apply()

    var opacity: Float
        get() = prefs.getFloat("OPACITY", 0.3f)
        set(value) = prefs.edit().putFloat("OPACITY", value).apply()
        
    var showFloatingButton: Boolean
        get() = prefs.getBoolean("SHOW_FLOATING_BUTTON", true)
        set(value) = prefs.edit().putBoolean("SHOW_FLOATING_BUTTON", value).apply()

    var animationSpeed: Float
        get() = prefs.getFloat("ANIMATION_SPEED", 1.0f)
        set(value) = prefs.edit().putFloat("ANIMATION_SPEED", value).apply()

    var color1Sharpness: Float
        get() = prefs.getFloat("COLOR1_SHARPNESS", 0.5f)
        set(value) = prefs.edit().putFloat("COLOR1_SHARPNESS", value).apply()

    var color2Sharpness: Float
        get() = prefs.getFloat("COLOR2_SHARPNESS", 0.5f)
        set(value) = prefs.edit().putFloat("COLOR2_SHARPNESS", value).apply()

    var activeApps: String
        get() = prefs.getString("ACTIVE_APPS", "com.songbookpro.songbookpro,com.amazon.kindle,org.mozilla.firefox,com.google.android.apps.docs") ?: ""
        set(value) = prefs.edit().putString("ACTIVE_APPS", value).apply()

    var lastYPosition: Float
        get() = prefs.getFloat("LAST_Y_POS", 500f)
        set(value) = prefs.edit().putFloat("LAST_Y_POS", value).apply()
        
    var lastXPosition: Float
        get() = prefs.getFloat("LAST_X_POS", 500f)
        set(value) = prefs.edit().putFloat("LAST_X_POS", value).apply()
}
