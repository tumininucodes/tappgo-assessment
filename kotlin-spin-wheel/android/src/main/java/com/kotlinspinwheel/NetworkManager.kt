package com.kotlinspinwheel

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SpinWheelConfig(
    val data: List<WidgetData>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WidgetData(
    val id: String,
    val network: NetworkConfig,
    val wheel: WheelConfig
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class NetworkConfig(
    val attributes: NetworkAttributes,
    val assets: NetworkAssets
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class NetworkAttributes(
    val refreshInterval: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class NetworkAssets(
    val host: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WheelConfig(
    val rotation: RotationConfig,
    val assets: WheelAssets
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RotationConfig(
    val minimumSpins: Int,
    val maximumSpins: Int,
    val duration: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WheelAssets(
    val bg: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String
)

class NetworkManager(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    private val manualAssetMap = mapOf(
        "bg.jpeg" to "1drONfH3aw-JwmVJceDhtvaUuaF1dHfRy",
        "wheel-frame.png" to "1hXNkF1IHpGQD8gFIKmkWpgpxtYyDxGwE",
        "wheel-spin.png" to "1HUxVWwUx-XTqJH2Fls17kKfeFdb_QaVY",
        "wheel.png" to "1eo3s4n1AeIiSVHCzPXr1B1nOPdO9cuj_"
    )


    suspend fun fetchAndDownloadAssets(configUrl: String): SpinWheelConfig? = withContext(Dispatchers.IO) {
        try {
            val directJsonUrl = toDirectDownloadUrl(configUrl)
            val request = Request.Builder().url(directJsonUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string() ?: return@withContext null
                val config = json.decodeFromString<SpinWheelConfig>(responseData)
                
                val widgetData = config.data.firstOrNull() ?: return@withContext null
                
                val host = widgetData.network.assets.host
                val assets = widgetData.wheel.assets

                downloadFile(buildUrl(host, assets.bg), "bg.png")
                downloadFile(buildUrl(host, assets.wheelFrame), "wheel-frame.png")
                downloadFile(buildUrl(host, assets.wheelSpin), "wheel-spin.png")
                downloadFile(buildUrl(host, assets.wheel), "wheel.png")

                return@withContext config
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun buildUrl(host: String, assetPath: String): String {
        if (host.contains("drive.google.com")) {
            manualAssetMap[assetPath]?.let { id ->
                return "https://drive.google.com/uc?export=download&id=$id"
            }
        }

        val baseUrl = if (host.endsWith("/")) host else "$host/"
        return baseUrl + assetPath
    }

    private fun toDirectDownloadUrl(url: String): String {
        if (url.contains("drive.google.com") && url.contains("/file/d/")) {
            val id = url.substringAfter("/file/d/").substringBefore("/")
            return "https://drive.google.com/uc?export=download&id=$id"
        }
        return url
    }

    private fun downloadFile(url: String, filename: String) {
        if (url.isEmpty()) return
        
        val cachedFile = File(context.filesDir, filename)
        if (cachedFile.exists() && cachedFile.length() > 0) {
            println("SpinWheel: Cached: $filename (skipping download)")
            return
        }

        println("SpinWheel: Downloading: $url -> $filename")
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response: Response ->
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { inputStream ->
                        val file = File(context.filesDir, filename)
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } else {
                    println("SpinWheel: Failed to download $url: ${response.code}")
                }
            }
        } catch (e: Exception) {
            println("SpinWheel: Error downloading $url: ${e.message}")
        }
    }
}
