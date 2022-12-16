package com.component

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.component.base.AppLifecycleObserve
import java.util.*

/**
 * Created by yan_x
 * 这里只做一件事监听生命周期
 * @date 2021/11/8/008 10:43
 * @description
 */
open class CommonApp : Application() {

    private var activityAccount = 0

    /**
     * 当前activity是否销毁完
     */
    private var activityCreated = 0


    private var activityOnResume = 0

    /**
     * 获取各个模块
     */
    private var moduleList: List<AppLifecycleObserve>? = null

    private var activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity) {
                activityOnResume--
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
                activityOnResume++
            }
        }


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
            onDestroy()
        }
    }

    /**
     * 是否处于后台运行
     * @return Boolean
     */
    fun isBackgrounds(): Boolean {
        return activityOnResume == 0
    }

    open fun onDestroy() {

    }

}