package com.mvvm.logcat

import android.util.Log
import com.dianping.logan.SendLogRunnable
import com.mvvm.logcat.utils.AESUtils
import com.mvvm.logcat.utils.LogFileUtils
import java.io.File

/**
 * Created by yan_x
 * @date 2021/11/13/013 13:46
 * @description
 */
class UploadLogFile : SendLogRunnable() {
    override fun sendLog(logFile: File?) {
        val file= LogFileUtils.bytes2File(AESUtils.decryptData("0123456789012345", LogFileUtils.file2Bytes(logFile?.absolutePath)),logFile?.absolutePath)
        Log.e("jie mie de wenjian",file.absolutePath)

    }
}