package com.example.freshcontroll.util

import android.util.Log
import com.example.freshcontroll.BuildConfig

/**
 * Componente centralizado de logging para la aplicación.
 * Sigue principios de Clean Architecture al abstraer el mecanismo de log
 * y permite controlar la salida según el tipo de build (Debug/Release).
 */
object FreshLogger {

    private const val GLOBAL_TAG = "FreshControll_Log"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "DEBUG: $message")
        }
    }

    fun i(tag: String, message: String) {
        Log.i(tag, "INFO: $message")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, "ERROR: $message", throwable)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, "WARNING: $message")
    }

    /**
     * Log específico para peticiones de red/Firestore
     */
    fun network(service: String, action: String, details: String) {
        if (BuildConfig.DEBUG) {
            Log.d("FreshNetwork", "📡 [$service] -> $action | Details: $details")
        }
    }
}