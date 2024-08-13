package com.ca.router_compiler

import android.app.Application
import android.util.Log
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
    fun init(context: Application) {
        try {
            val set = ClassUtils.getFileNameByPackageName(context, PAGE_NAME)
            set?.map {
                val target = Class.forName(it)
                Log.e("MainActivitytarget", "" + target)
                target.getMethod("loadRouterMap", Map::class.java).invoke(target.newInstance(), map)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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