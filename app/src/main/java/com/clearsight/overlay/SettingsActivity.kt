package com.clearsight.overlay

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class SettingsActivity : Activity() {

    private lateinit var prefs: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesHelper(this)
        
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        scroll.addView(layout)

        val title = TextView(this).apply {
            text = "ClearSight Settings"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)

        // Theme Spinner
        val themes = arrayOf("tint", "ruler", "ruler_v", "slit", "slit_v", "spotlight", "crosshair", "reading_window", "column", "dual_anchor", 
            "neon/grid_scan", "neon/pulse_ring", "neon/laser_sweep", "neon/cyber_wave", "neon/data_stream")
        val spinner = Spinner(this).apply {
            val adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, themes)
            this.adapter = adapter
            val currentIdx = themes.indexOf(prefs.themeType).let { if (it == -1) 0 else it }
            setSelection(currentIdx)
            
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    prefs.themeType = themes[position]
                    sendUpdateBroadcast()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        layout.addView(TextView(this).apply { text = "Select Movement Theme:" })
        layout.addView(spinner)

        // Opacity Slider
        addSeekBar(layout, "Overlay Opacity:", (prefs.opacity * 100).toInt(), 100) { progress ->
            prefs.opacity = progress / 100f
            sendUpdateBroadcast()
        }

        // Speed Slider
        addSeekBar(layout, "Animation Speed:", (prefs.animationSpeed * 50).toInt(), 100) { progress ->
            prefs.animationSpeed = progress / 50f
            sendUpdateBroadcast()
        }

        // Color 1 Sharpness
        addSeekBar(layout, "Color 1 Sharpness:", (prefs.color1Sharpness * 100).toInt(), 100) { progress ->
            prefs.color1Sharpness = progress / 100f
            sendUpdateBroadcast()
        }

        // Color 2 Sharpness
        addSeekBar(layout, "Color 2 Sharpness:", (prefs.color2Sharpness * 100).toInt(), 100) { progress ->
            prefs.color2Sharpness = progress / 100f
            sendUpdateBroadcast()
        }

        // Permissions
        layout.addView(TextView(this).apply { text = "Permissions:"; setPadding(0, 32, 0, 0) })
        
        layout.addView(Button(this).apply {
            text = "Grant Overlay Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })

        layout.addView(Button(this).apply {
            text = "Enable Accessibility Service"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })
        
        // Show Button Checkbox
        layout.addView(CheckBox(this).apply {
            text = "Show Floating Menu Bubble"
            isChecked = prefs.showFloatingButton
            setOnCheckedChangeListener { _, isChecked ->
                prefs.showFloatingButton = isChecked
                sendUpdateBroadcast()
                if(isChecked) {
                    startService(Intent(this@SettingsActivity, FloatingMenuService::class.java))
                } else {
                    stopService(Intent(this@SettingsActivity, FloatingMenuService::class.java))
                }
            }
        })

        // Manual Controls
        layout.addView(Button(this).apply {
            text = "Start Motion Overlay (Test)"
            setOnClickListener {
                val intent = Intent(this@SettingsActivity, OverlayService::class.java)
                intent.putExtra("ACTION", "SHOW")
                startService(intent)
            }
        })

        layout.addView(Button(this).apply {
            text = "Stop Motion Overlay"
            setOnClickListener {
                val intent = Intent(this@SettingsActivity, OverlayService::class.java)
                intent.putExtra("ACTION", "HIDE")
                startService(intent)
            }
        })

        setContentView(scroll)
        CSLog.lifecycle("SettingsActivity", "onCreate")
    }

    private fun addSeekBar(layout: LinearLayout, label: String, current: Int, maxVal: Int, onUpdate: (Int) -> Unit) {
        layout.addView(TextView(this).apply { text = label; setPadding(0, 16, 0, 0) })
        val bar = SeekBar(this).apply {
            max = maxVal
            progress = current
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { onUpdate(p) }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        layout.addView(bar)
    }
    
    private fun sendUpdateBroadcast() {
        sendBroadcast(Intent("com.clearsight.overlay.UPDATE_SETTINGS"))
    }
}
