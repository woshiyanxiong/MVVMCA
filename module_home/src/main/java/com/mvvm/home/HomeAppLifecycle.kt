package com.mvvm.home

import android.app.Application
import android.util.Log
import com.common.base.AppLifecycleObserve
import com.google.auto.service.AutoService

/**
 * Created by yan_x
 * @date 2021/11/30/030 17:52
 * @description 注册home模块的生命周期
 */
@AutoService(AppLifecycleObserve::class)
class HomeAppLifecycle : AppLifecycleObserve {
    override fun onCreate(app: Application) {
        Log.i("HomeAppLifecycle", "onCreate")
    }

    override fun onDestroy() {
        Log.i("HomeAppLifecycle", "onDestroy")
    }
}