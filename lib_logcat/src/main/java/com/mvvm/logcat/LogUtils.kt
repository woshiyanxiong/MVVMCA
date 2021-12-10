package com.mvvm.logcat

import android.content.Context
import android.util.Log
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import java.io.File

/**
 * Created by yan_x
 * @date 2021/11/13/013 10:44
 * @description
 */
object LogUtils {
    private var logPath=""
    fun init(context: Context){
        logPath =context.getExternalFilesDir(null)
            ?.absolutePath + File.separator.toString() + "logan_v1"
        Log.e("日志地址",logPath)
        val config: LoganConfig = LoganConfig.Builder()
            .setCachePath(context.filesDir.absolutePath)
            .setPath(logPath)
            .setEncryptKey16("0123456789012345".toByteArray())
            .setEncryptIV16("0123456789012345".toByteArray())
            .setMaxFile(10)
            .setDay(3)
            .build()
        Logan.init(config)
        Logan.setDebug(false)

    }

    fun d(tag: String, msg: String) {
        Logan.w(msg,3)
//        Log.e(tag,msg)
    }

    fun i(tag: String, msg: String) {
        Log.e(tag,msg)
    }

    fun e(tag: String, msg: String) {

    }

    fun v(tag: String, msg: String) {

    }
    fun logAll(){
        val map: Map<String, Long> = Logan.getAllFilesInfo()
        Log.e("所有日志",map.toString())
    }
}