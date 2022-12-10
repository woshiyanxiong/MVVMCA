package com.component.base

import android.app.Application

/**
 * Created by yan_x
 * @date 2021/11/30/030 17:49
 * @description 模块生命周期
 */
interface AppLifecycleObserve {
    /**
     * 初始化
     * @param app Application
     */
    fun onCreate(app:Application)

    /**
     * 销毁
     */
    fun onDestroy()
}