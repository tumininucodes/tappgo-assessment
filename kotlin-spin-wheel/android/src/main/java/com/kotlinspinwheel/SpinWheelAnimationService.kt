package com.kotlinspinwheel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Drives the spin wheel animation. Spin count and duration come from the remote config. */
class SpinWheelAnimationService : Service() {

    companion object {
        const val EXTRA_INIT_ONLY = "init_only"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "spin_wheel_service"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Spin Wheel", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Spin wheel widget update" }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Spin Wheel")
            .setContentText("Updating widget...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var currentAngle = 0f
    private var targetAngle = 0f
    private var frameIntervalMs = 50L  // ~20fps; overridden by config.duration
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resetRunnable: Runnable? = null

    private val prizes = listOf(
        "🎁 Free Item", "🌟 10% Off", "🎉 Grand Prize!",
        "💎 Bonus", "🏆 Jackpot!", "🎯 Try Again"
    )

    private val animationRunnable = object : Runnable {
        override fun run() {
            val remaining = targetAngle - currentAngle
            if (remaining > 1f) {
                val delta = (remaining * 0.10f).coerceAtLeast(3f)
                currentAngle += delta
                pushUpdate(currentAngle, "", "Spinning...", isSpinning = true)
                handler.postDelayed(this, frameIntervalMs)
            } else {
                currentAngle = targetAngle % 360f
                val prize = prizes[Random.nextInt(prizes.size)]
                pushUpdate(currentAngle, prize, "Tap to Spin!", isSpinning = false)
                println("SpinWheelWidget: Done. Prize=$prize, finalAngle=${currentAngle}°")

                SpinWheelWidgetProvider.setSpinning(applicationContext, false)

                val reset = Runnable {
                    pushUpdate(currentAngle, "", "Tap to Spin!", isSpinning = false)
                    stopSelf()
                }
                resetRunnable = reset
                handler.postDelayed(reset, 5000L)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Android 12+: must call startForeground within 5s of startForegroundService()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Init-only mode: download assets and refresh the widget, no animation
        if (intent?.getBooleanExtra(EXTRA_INIT_ONLY, false) == true) {
            println("SpinWheelWidget: Init-only — pre-fetching assets...")
            CoroutineScope(Dispatchers.IO).launch {
                val networkManager = NetworkManager(applicationContext)
                networkManager.fetchAndDownloadAssets(SpinWheelWidgetProvider.CONFIG_URL)
                println("SpinWheelWidget: Assets ready.")
                val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                SpinWheelWidgetProvider.updateWidget(
                    applicationContext, appWidgetManager, appWidgetId, 0f, ""
                )
                stopSelf()
            }
            return START_NOT_STICKY
        }

        // Cancel any pending reset from a previous spin before starting a new one
        resetRunnable?.let { handler.removeCallbacks(it) }
        resetRunnable = null

        // Acquire spin lock — prevents double-tap re-entry
        SpinWheelWidgetProvider.setSpinning(applicationContext, true)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        SpinWheelWidgetProvider.updateWidget(
            this, appWidgetManager, appWidgetId, currentAngle, "", "⏳ Loading...", isSpinning = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            val networkManager = NetworkManager(applicationContext)
            val config = networkManager.fetchAndDownloadAssets(SpinWheelWidgetProvider.CONFIG_URL)

            val widgetData = config?.data?.firstOrNull()
            if (widgetData != null) {
                val rotation = widgetData.wheel.rotation
                val minSpins = rotation.minimumSpins.coerceAtLeast(1)
                val maxSpins = rotation.maximumSpins.coerceAtLeast(minSpins + 1)
                val durationMs = rotation.duration.toLong().coerceAtLeast(500L)

                val fullSpins = (minSpins + Random.nextInt(maxSpins - minSpins)) * 360f
                val landingOffset = Random.nextFloat() * 360f
                targetAngle = currentAngle + fullSpins + landingOffset

                // Derive frame interval from total duration
                val expectedFrames = (fullSpins / 10f).toInt().coerceAtLeast(1)
                frameIntervalMs = (durationMs / expectedFrames).coerceIn(30L, 100L)

                println("SpinWheelWidget: Config applied — spins=${fullSpins}°, duration=${durationMs}ms, frameInterval=${frameIntervalMs}ms")
            } else {
                // Fallback defaults
                val fullSpins = (5 + Random.nextInt(6)) * 360f
                val landingOffset = Random.nextFloat() * 360f
                targetAngle = currentAngle + fullSpins + landingOffset
                println("SpinWheelWidget: Config unavailable, using defaults.")
            }

            handler.post(animationRunnable)
        }

        return START_NOT_STICKY
    }

    private fun pushUpdate(angle: Float, prizeText: String, buttonText: String, isSpinning: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        SpinWheelWidgetProvider.updateWidget(
            this, appWidgetManager, appWidgetId, angle, prizeText, buttonText, isSpinning
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        // Safety net: always release the lock when the service ends
        SpinWheelWidgetProvider.setSpinning(applicationContext, false)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
