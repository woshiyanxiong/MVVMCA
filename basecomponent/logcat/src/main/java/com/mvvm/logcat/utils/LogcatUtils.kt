package com.mvvm.logcat.utils

import android.content.Context
import android.content.Intent
import com.mvvm.logcat.FloatingLogcatService

/**
 * Created by yan_x
 * @date 2021/12/17/017 16:00
 * @description
 */
object LogcatUtils {
    fun stop(context: Context){
        context.stopService(Intent(context,FloatingLogcatService::class.java))
    }
}