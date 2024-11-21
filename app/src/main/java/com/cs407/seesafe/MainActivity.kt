package com.cs407.seesafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    val notificationHelper = NotificationHelper.getInstance()
    fun notifyHelpers(requestSender: String, requestDetails: String) {
        notificationHelper.setNotificationContent(requestSender, requestDetails)
        notificationHelper.showNotification(this)
    }
    fun notifyBlindUser(helperName: String, userId: Int) {
        val message = "$helperName has accepted your request for help."
        notificationHelper.setNotificationContent("Request Accepted", message)
        notificationHelper.showNotification(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, getString(R.string.please_allow_notification), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val topButton: Button = findViewById(R.id.topButton)
        topButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val bottomButton: Button = findViewById(R.id.bottomButton)
        bottomButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}