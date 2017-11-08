package com.ryuta46.nemapitestandroid.util

import android.util.TimeUtils
import java.util.*

class TimeUtil {
    companion object {

        fun currentTimeFromOrigin(): Int {
            // NEM のネメシスブロック生成日時は 2015/03/29 0:06:25
            // この日時から現在時刻の差分を得る
            val origin = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            origin.set(2015, 2, 29, 0, 6, 25)

            val current = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            // ミリ秒 -> 秒に変換して返す
            return Math.floor(((current.time.time - origin.time.time) / 1000.0)).toInt()
        }
    }
}