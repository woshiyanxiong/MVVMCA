package com.common.base

import android.app.Application

/**
 * Created by yan_x
 * @date 2021/11/30/030 17:49
 * @description
 */
interface AppLifecycleObserve {
    fun onCreate(app:Application)
    fun onDestroy()
}