package com.example.screentranslator

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val captureCode = 40
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showSetup() }
    private fun showSetup() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48,48,48,48) }
        box.addView(TextView(this).apply { text = "Screen Translator\n\nA floating translator that lives beside whatever app you are using."; textSize = 24f })
        box.addView(TextView(this).apply { text = "\nFirst, allow the overlay permission. Then allow screen capture. The translator bubble will appear over other apps."; textSize = 16f })
        box.addView(Button(this).apply { text = "1. Allow overlay"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } })
        box.addView(Button(this).apply { text = "2. Start translator"; setOnClickListener { requestCapture() } })
        box.addView(Button(this).apply { text = "Stop translator"; setOnClickListener { stopService(Intent(this@MainActivity, TranslatorOverlayService::class.java)) } })
        setContentView(box)
    }
    private fun requestCapture() { val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager; startActivityForResult(mgr.createScreenCaptureIntent(), captureCode) }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode,resultCode,data); if (requestCode == captureCode && resultCode == Activity.RESULT_OK && data != null) { startForegroundService(Intent(this, TranslatorOverlayService::class.java).apply { putExtra("resultCode", resultCode); putExtra("data", data) }); Toast.makeText(this,"Translator started",Toast.LENGTH_SHORT).show() } }
}
