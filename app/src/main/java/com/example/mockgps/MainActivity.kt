package com.example.mockgps

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) perms += Manifest.permission.POST_NOTIFICATIONS
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), 1)

        val latField = findViewById<EditText>(R.id.lat)
        val lonField = findViewById<EditText>(R.id.lon)

        findViewById<Button>(R.id.start).setOnClickListener {
            val lat = latField.text.toString().toDoubleOrNull()
            val lon = lonField.text.toString().toDoubleOrNull()
            if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                Toast.makeText(this, "Неверные координаты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this, MockService::class.java)
                .putExtra("lat", lat).putExtra("lon", lon)
            ContextCompat.startForegroundService(this, i)
        }

        findViewById<Button>(R.id.stop).setOnClickListener {
            stopService(Intent(this, MockService::class.java))
        }
    }
}
