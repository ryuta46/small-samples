package com.ryuta46.networkprofiler

import android.util.Log

class Logger(val tag: String) {
    companion object {
        val LOG_LEVEL_NONE = 0
        val LOG_LEVEL_ERROR = 1
        val LOG_LEVEL_WARN = 2
        val LOG_LEVEL_INFO = 3
        val LOG_LEVEL_DEBUG = 4
        val LOG_LEVEL_VERBOSE = 5

        private val TAG = "NetworkProfiler"
        var level = LOG_LEVEL_NONE
    }

    fun e(message: String) {
        if (level >= LOG_LEVEL_ERROR) Log.e(TAG, "|ERR|$tag|$message")
    }

    fun w(message: String) {
        if (level >= LOG_LEVEL_WARN) Log.w(TAG, "|WRN|$tag|$message")
    }

    fun i(message: String) {
        if (level >= LOG_LEVEL_INFO) Log.i(TAG, "|INF|$tag|$message")
    }

    fun d(message: String) {
        if (level >= LOG_LEVEL_DEBUG) Log.d(TAG, "|DBG|$tag|$message")
    }

    fun v(message: String) {
        if (level >= LOG_LEVEL_VERBOSE) Log.v(TAG, "|VRB|$tag|$message")
    }

    inline fun <T> trace(body: () -> T): T {
        val callerName = if (level >= LOG_LEVEL_DEBUG) {
            Throwable().stackTrace[0].methodName
        } else {
            null
        }

        try {
            callerName?.let {
                d("$callerName start")
            }
            return body()
        }
        finally {
            callerName?.let {
                d("$callerName end")
            }
        }
    }
}

