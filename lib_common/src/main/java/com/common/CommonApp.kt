package com.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.common.base.AppLifecycleObserve
import java.util.*

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:43
 * @description
 */
open class CommonApp : Application() {

    private var activityAccount = 0

    /**
     * 当前activity是否销毁完
     */
    private var activityCreated = 0

    /**
     * 获取各个模块
     */
    private var moduleList: List<AppLifecycleObserve>? = null

    private var activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity) {

            }

            override fun onActivityStarted(p0: Activity) {
                activityAccount++
            }

            override fun onActivityDestroyed(p0: Activity) {
                activityCreated--
                destroy()
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

            }

            override fun onActivityStopped(p0: Activity) {
                activityAccount--
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                activityCreated++
            }

            override fun onActivityResumed(p0: Activity) {

            }
        }

    /**
     * 是否在后台
     */
    fun isBackground(): Boolean = activityAccount == 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        initModuleApplication()
    }

    /**
     * 注册各个模块的生命周期
     */
    private fun initModuleApplication() {
        moduleList =
            ServiceLoader.load(AppLifecycleObserve::class.java, javaClass.classLoader).toList()
        moduleList?.forEach { module ->
            module.onCreate(this)
        }

    }

    /**
     * 注销
     */
    private fun destroy() {
        if (activityCreated == 0) {
            moduleList?.forEach {
                it.onDestroy()
            }
        }
    }

}