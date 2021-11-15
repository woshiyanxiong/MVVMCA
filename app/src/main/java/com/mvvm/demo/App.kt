package com.mvvm.demo

import android.app.Application
import com.mvvm.logcat.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import com.mvvm.logcat.LogUtils


/**
 * Created by yan_x
 * @date 2021/11/10/010 15:24
 * @description
 */
@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.getInstance().init(this)
        LogUtils.init(this)
    }
}