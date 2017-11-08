package com.ryuta46.nemapitestandroid.util

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import java.util.*

class PreferenceUtil {
    companion object {
        val KEY_PUBLIC_KEY = "publicKey"
        val KEY_PRIVATE_KEY = "privateKey"

        fun loadString(context: Context, key: String, defaultValue: String) : String {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getString(key, defaultValue)
        }

        fun saveString(context: Context, key: String, value: String) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(key, value)
            editor.apply()
        }


        fun loadByteArray(context: Context, key: String, defaultValue: ByteArray) : ByteArray {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val stringValue = pref.getString(key, "")

            return if (stringValue.isEmpty()) {
                defaultValue
            } else {
                Base64.decode(stringValue, Base64.DEFAULT)
            }
        }

        fun saveByteArray(context: Context, key: String, value: ByteArray) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(key, Base64.encodeToString(value, Base64.DEFAULT))
            editor.apply()
        }
    }
}