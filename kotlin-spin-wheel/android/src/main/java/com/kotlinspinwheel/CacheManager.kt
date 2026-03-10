package com.kotlinspinwheel

import android.content.Context
import android.content.SharedPreferences

class CacheManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastFetchTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_LAST_FETCH, timeMillis).apply()
    }

    fun getLastFetchTime(): Long {
        return prefs.getLong(KEY_LAST_FETCH, 0L)
    }

    fun shouldRefresh(refreshIntervalSeconds: Int): Boolean {
        val lastFetch = getLastFetchTime()
        if (lastFetch == 0L) return true

        val now = System.currentTimeMillis()
        val intervalMillis = refreshIntervalSeconds * 1000L
        return (now - lastFetch) > intervalMillis
    }

    companion object {
        private const val PREFS_NAME = "SpinWheelCachePrefs"
        private const val KEY_LAST_FETCH = "last_fetch_time"
    }
}
