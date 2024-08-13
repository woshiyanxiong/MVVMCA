package com.ca.router_compiler

import android.app.Application
import android.os.Bundle
import com.alibaba.android.arouter.exception.InitException
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter

/**
 * <p>创建时间：2024/8/13/013</p>
 *
 * @author yanxiong
 */
class CARouterApi private constructor() {
    companion object {
        private var instance: CARouterApi? = null

        private var initAfter = false
        fun init(context: Application) {
            ARouter.init(context)
            ARouter.openLog()
            RouterMap.init(context)
            initAfter = true
        }

        fun getInstance(): CARouterApi {
            if (!initAfter) {
                throw InitException("CARouterApi::Init::Invoke init(context) first!")
            }
            if (instance == null) {
                instance = CARouterApi()
            }
            return instance!!
        }
    }

    /**
     * 跳转页面
     * @param url String
     */
    fun navigation(url: String, bundle: (bundle: Postcard) -> Unit = {}) {
        RouterMap.navigationMapRouter(url) {
            ARouter.getInstance().build(it).apply {
                bundle.invoke(this)
            }.navigation()
        }
    }

    fun <T> getByClass(clazz: Class<T>): T? {
        return kotlin.runCatching {
            return ARouter.getInstance().navigation(clazz)
        }.getOrNull()
    }


}