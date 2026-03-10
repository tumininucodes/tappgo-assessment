package com.kotlinspinwheel

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import kotlin.random.Random

class SpinWheelView(context: Context) : FrameLayout(context) {

    private val networkManager = NetworkManager(context)
    private val cacheManager = CacheManager(context)

    private val backgroundView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    
    private val wheelView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }
    
    private val frameView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }
    
    private val spinButtonView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private val statusTextView = TextView(context).apply {
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        textSize = 16f
        text = "Loading..."
    }

    private var isSpinning = false
    private var currentConfig: SpinWheelConfig? = null

    init {
        val centerLayoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )

        addView(backgroundView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(wheelView, centerLayoutParams)
        addView(frameView, centerLayoutParams)
        addView(spinButtonView, centerLayoutParams)
        addView(statusTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        spinButtonView.setOnClickListener {
            if (!isSpinning && currentConfig != null) {
                spinWheel()
            }
        }
    }

    fun loadConfig(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            statusTextView.visibility = View.VISIBLE
            statusTextView.text = "Fetching config..."

            val config = networkManager.fetchAndDownloadAssets(url)
            if (config != null) {
                currentConfig = config
                cacheManager.saveLastFetchTime(System.currentTimeMillis())
                loadLocalImages()
                statusTextView.visibility = View.GONE
            } else {
                statusTextView.text = "Failed to load config."
            }
        }
    }

    private suspend fun loadLocalImages() = withContext(Dispatchers.IO) {
        val files = mapOf(
            "bg.png" to backgroundView,
            "wheel.png" to wheelView,
            "wheel-frame.png" to frameView,
            "wheel-spin.png" to spinButtonView
        )

        withContext(Dispatchers.Main) {
            files.forEach { (filename, imageView) ->
                val file = File(context.filesDir, filename)
                if (file.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        } else {
                            println("SpinWheel: Failed to decode bitmap: $filename")
                        }
                    } catch (e: Exception) {
                        println("SpinWheel: Error loading $filename: ${e.message}")
                    }
                } else {
                    println("SpinWheel: Asset file not found in local cache: $filename")
                }
            }
        }
    }

    private fun spinWheel() {
        isSpinning = true
        
        val widgetData = currentConfig?.data?.firstOrNull() ?: return
        val rotationConfig = widgetData.wheel.rotation
        
        val numSpins = Random.nextInt(rotationConfig.minimumSpins, rotationConfig.maximumSpins + 1)
        val randomDegree = Random.nextFloat() * 360f
        
        val totalRotation = (numSpins * 360f) + randomDegree

        val rotateAnimation = RotateAnimation(
            0f, totalRotation,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = rotationConfig.duration.toLong()
            fillAfter = true
            interpolator = DecelerateInterpolator()
            
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    isSpinning = false
                    println("SpinWheel: Wheel finished spinning.")
                }
            })
        }

        wheelView.startAnimation(rotateAnimation)
    }
}
