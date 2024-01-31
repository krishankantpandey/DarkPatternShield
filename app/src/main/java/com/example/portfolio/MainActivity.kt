package com.example.portfolio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Accessibility settings were updated, check again and start the service if enabled
                if (isAccessibilityServiceEnabled(this, "com.example.portfolio/.MyAccessibilityService")) {
                    startService(Intent(this, FloatWidgetService::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Accessibility permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonRegister = findViewById<Button>(R.id.btnRegister)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val buttonWidget = findViewById<Button>(R.id.createBtn)
        buttonWidget.setOnClickListener {
            val intent = Intent(this,FloatWidgetService::class.java)
            startService(intent)
        }

        // Check if accessibility service is enabled when the app is opened
        if (!isAccessibilityServiceEnabled(this, "com.example.portfolio/.MyAccessibilityService")) {
            // Accessibility service permission is not granted, open accessibility settings
            val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            requestPermissionLauncher.launch(accessibilityIntent)
        } else {
            // Accessibility service is enabled, start the FloatWidgetService
            startService(Intent(this, FloatWidgetService::class.java))
            finish()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
        val contentResolver = context.contentResolver
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        return enabledServices?.contains(serviceName) == true
    }
}
