package com.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.common.helper.image.ImageLoad
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:43
 * @description
 */

class App: Application() {

    override fun onCreate() {
        super.onCreate()

    }

    private var activityAccount = 0

    private var activityLifecycleCallbacks: ActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity) {

        }

        override fun onActivityStarted(p0: Activity) {
            activityAccount++
        }

        override fun onActivityDestroyed(p0: Activity) {

        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

        }

        override fun onActivityStopped(p0: Activity) {
            activityAccount--
        }

        override fun onActivityCreated(p0: Activity, p1: Bundle?) {

        }

        override fun onActivityResumed(p0: Activity) {

        }
    }

    /**
     * 是否在后台
     */
    fun isBackground():Boolean=activityAccount==0
}