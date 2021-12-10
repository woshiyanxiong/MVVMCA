package com.mvvm.demo

import com.common.CommonApp
import com.mvvm.logcat.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import com.mvvm.logcat.LogUtils


/**
 * Created by yan_x
 * @date 2021/11/10/010 15:24
 * @description 可以用CommonApp 也可不用
 */
@HiltAndroidApp
class App: CommonApp() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.getInstance().init(this)
        LogUtils.init(this)
    }
}