package com.example.mockgps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class MockService : Service() {
    private val provider = LocationManager.GPS_PROVIDER
    private lateinit var lm: LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var lat = 0.0
    private var lon = 0.0

    private val tick = object : Runnable {
        override fun run() {
            pushLocation()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("mock", "Mock GPS", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
        lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0

        val n = NotificationCompat.Builder(this, "mock")
            .setContentTitle("Подмена местоположения активна")
            .setContentText("%.5f, %.5f".format(lat, lon))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        val type = if (Build.VERSION.SDK_INT >= 29)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        ServiceCompat.startForeground(this, 1, n, type)

        try {
            try { lm.removeTestProvider(provider) } catch (_: Exception) {}
            lm.addTestProvider(
                provider, false, false, false, false, true, true, true,
                Criteria.POWER_LOW, Criteria.ACCURACY_FINE
            )
            lm.setTestProviderEnabled(provider, true)
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Выберите это приложение в Настройки → Для разработчиков → Приложение для фиктивных местоположений",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return START_NOT_STICKY
        }

        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    private fun pushLocation() {
        val loc = Location(provider).apply {
            latitude = lat
            longitude = lon
            altitude = 0.0
            accuracy = 5f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        try { lm.setTestProviderLocation(provider, loc) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        try {
            lm.setTestProviderEnabled(provider, false)
            lm.removeTestProvider(provider)
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
