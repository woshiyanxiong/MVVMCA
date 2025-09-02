package com.mvvm.logcat

import android.content.Context
import android.util.Log
import com.dianping.logan.BuildConfig
import com.dianping.logan.Logan
import com.elvishew.xlog.XLog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * Created by yan_x
 * @date 2021/11/13/013 10:44
 * @description
 */
object LogUtils {
    private var logPath = ""
    private val LINE_SEPARATOR: String = System.getProperty("line.separator")
    fun init(context: Context) {
        logPath = context.getExternalFilesDir(null)
            ?.absolutePath + File.separator.toString() + "logan_v1"
        Log.e("日志地址", logPath)
    }

    fun d(tag: String, msg: String) {
        XLog.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        XLog.i(tag, msg)
    }

    fun e(tag: String?, msg: String?) {
        XLog.e(tag, ""+msg)
    }

    fun e( msg: String?) {
        XLog.e("error", ""+msg)
    }

    fun v(tag: String, msg: String) {
        XLog.v(tag, msg)
    }

    fun gson(msg: String) {
        val jsonObject = JSONObject(msg)
        XLog.json(jsonObject.toString())
    }

    @Synchronized
    fun netGson(tag: String?, msg: String, headString: String) {
        if (BuildConfig.DEBUG)
            printJson(tag, msg, headString)
    }


    fun logAll() {

    }

    fun getLocalFle(): String {
        return logPath
    }

    fun ffff() {
        Logan.s(arrayOf("2021-12-17"), UploadLogFile());
    }

    fun logRequest(tag: String?, msg: String, type: Int) {
        if (type == 0) {
            printLine(tag, true)
            Log.e(tag, msg)
        }
        if (type == 1) {
            Log.e(tag, msg)
            printLine(tag, false)
        }
        if (type == 2) {
            Log.e(tag, "║ ${msg}")
        }
    }

    private fun printLine(tag: String?, isTop: Boolean) {
        if (isTop) {
            Log.e(
                tag,
                "╔═══════════════════════════════════════════════════════════════════════════════════════"
            )
        } else {
            Log.e(
                tag,
                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
        }
    }

    private fun printJson(tag: String?, msg: String, headString: String) {
        var message: String = try {
            when {
                msg.startsWith("{") -> {
                    val jsonObject = JSONObject(msg)
                    jsonObject.toString(4) //最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
                }
                msg.startsWith("[") -> {
                    val jsonArray = JSONArray(msg)
                    jsonArray.toString(4)
                }
                else -> {
                    msg
                }
            }
        } catch (e: JSONException) {
            msg
        }
        printLine(tag, true)
        message = headString + LINE_SEPARATOR + message
        val lines: Array<String> = message.split(LINE_SEPARATOR).toTypedArray()
        for (line in lines) {
            Log.e(tag, "║ $line")
        }
        printLine(tag, false)
    }
}