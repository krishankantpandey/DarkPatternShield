package com.example.portfolio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val buttonSubmit = findViewById<Button>(R.id.btnSubmit)
        buttonSubmit.setOnClickListener {
            intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }
    }
}