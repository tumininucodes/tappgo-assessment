package com.kotlinspinwheel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import java.io.File

class SpinWheelWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_SPIN = "com.kotlinspinwheel.ACTION_SPIN"
        const val CONFIG_URL = "https://drive.google.com/uc?export=download&id=1VqSottVQmYfiAEYzJRSpfFaByvR-X4JY"
        const val PREFS_NAME = "SpinWheelWidgetPrefs"
        const val KEY_IS_SPINNING = "is_spinning"

        fun isSpinning(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_SPINNING, false)

        fun setSpinning(context: Context, spinning: Boolean) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_IS_SPINNING, spinning).apply()

        fun assetsExist(context: Context): Boolean {
            val required = listOf("bg.png", "wheel.png", "wheel-frame.png", "wheel-spin.png")
            return required.all { java.io.File(context.filesDir, it).exists() }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            rotation: Float,
            prizeText: String,
            buttonText: String = "Tap to Spin!",
            isSpinning: Boolean = false
        ) {
            val views = RemoteViews(context.packageName, R.layout.spin_wheel_widget_layout)

            val bgFile = File(context.filesDir, "bg.png")
            if (bgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(bgFile.absolutePath)
                if (bitmap != null) views.setImageViewBitmap(R.id.img_bg, bitmap)
            }

            val wheelFile = File(context.filesDir, "wheel.png")
            if (wheelFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(wheelFile.absolutePath)
                if (bitmap != null) views.setImageViewBitmap(R.id.img_wheel, bitmap)
            }

            val wheelSpinFile = File(context.filesDir, "wheel-spin.png")
            if (wheelSpinFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(wheelSpinFile.absolutePath)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.img_wheel_spin, bitmap)
                    views.setFloat(R.id.img_wheel_spin, "setRotation", rotation)
                }
            }

            val frameFile = File(context.filesDir, "wheel-frame.png")
            if (frameFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(frameFile.absolutePath)
                if (bitmap != null) views.setImageViewBitmap(R.id.img_frame, bitmap)
            }

            views.setTextViewText(R.id.txt_result, prizeText)
            views.setTextViewText(R.id.btn_spin, buttonText)
            views.setInt(
                R.id.btn_spin, "setBackgroundResource",
                if (isSpinning) R.drawable.btn_spin_disabled else R.drawable.btn_spin_normal
            )

            val spinIntent = Intent(context, SpinWheelWidgetProvider::class.java).apply {
                action = ACTION_SPIN
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, spinIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_spin, pendingIntent)
            views.setOnClickPendingIntent(R.id.img_frame, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, 0f, "")

            if (!assetsExist(context)) {
                println("SpinWheelWidget: Assets missing — starting pre-fetch.")
                val initIntent = Intent(context, SpinWheelAnimationService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(SpinWheelAnimationService.EXTRA_INIT_ONLY, true)
                }
                context.startForegroundService(initIntent)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SPIN) {
            if (isSpinning(context)) {
                println("SpinWheelWidget: Tap ignored — spin already in progress.")
                return
            }
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                println("SpinWheelWidget: Spin triggered for widgetId=$appWidgetId")
                val serviceIntent = Intent(context, SpinWheelAnimationService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
