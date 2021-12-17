package com.mvvm.demo

import com.common.CommonApp
import com.common.helper.image.ImageLoadConfig
import com.common.helper.loading.LoadingConfig
import com.mvvm.demo.config.AppLoadingView
import com.mvvm.demo.config.ImageLoadGlide
import com.mvvm.logcat.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import com.mvvm.logcat.LogUtils
import com.mvvm.logcat.utils.LogcatUtils


/**
 * Created by yan_x
 * @date 2021/11/10/010 15:24
 * @description 可以用CommonApp 也可不用
 */
@HiltAndroidApp
class App : CommonApp() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.getInstance().init(this)
        LogUtils.init(this)
//        LoadingConfig.initLoadingView(AppLoadingView())
        //图片加载引擎
        ImageLoadConfig.init(ImageLoadGlide())
    }
}