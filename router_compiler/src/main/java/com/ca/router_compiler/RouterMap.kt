package com.ca.router_compiler

import android.app.Application
import android.media.tv.TvContract.Channels.Logo
import android.util.Log
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.utils.ClassUtils

/**
 * 类的作用的简要阐述
 *
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/13/013</p>
 *
 * @author yanxiong
 */
object RouterMap {
    private val map = mutableMapOf<String, String>()

    private const val PAGE_NAME = "com.ca.map.router"

    private var pluginInit = false
    fun init(context: Application) {
        loadRouterMapPlugin()
        if (pluginInit) {
             Log.e("RouterMap","插件加载了路由映射")
        } else {
            try {
                val set = ClassUtils.getFileNameByPackageName(context, PAGE_NAME)
                set?.map {
                    Log.e("运行期间获取的地址",it)
                    register(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun loadRouterMapPlugin() {
        pluginInit = false
//        register("")
//        register("1")
//        register("2")
    }


    private fun register(className: String) {
        if (!pluginInit) {
            pluginInit = true
        }
        Log.e("获取的className",className)
        val target = Class.forName(className)
        target.getMethod("loadRouterMap", Map::class.java)
            .invoke(target.newInstance(), map)
    }


    fun navigationMapRouter(url: String, navigation: (url: String) -> Unit) {
        val newUrl = map[url]
        if (newUrl.isNullOrBlank()) {
            navigation.invoke(url)
        } else {
            navigation.invoke(newUrl)
        }
    }


}