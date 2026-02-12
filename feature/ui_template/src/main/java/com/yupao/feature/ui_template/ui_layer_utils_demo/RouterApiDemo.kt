package com.yupao.feature.ui_template.ui_layer_utils_demo

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import com.yupao.utils.system.RouterApi

/**
 * 展示了RouterApi的一些用法
 *
 * 创建时间：2025/9/18
 *
 * @author fc
 */
internal object RouterApiDemo {
    /**
     * 从后台、h5等拿到路由地址字符串进行跳转
     */
    fun route(routePath: String?) {
        if (routePath.isNullOrBlank()) return
        RouterApi.runUri(routePath)
    }

    /**
     * 跨模块通过接口调用来实现跳转
     */
    fun route() {
        RouterApi.getByClass(IResumeRouter::class.java)?.routeResume("")
    }
}

internal interface IResumeRouter : IProvider {
    fun routeResume(resumeId: String?)
}

@Route(path = "ui_template/page/router")
internal class IResumeRouterImpl : IResumeRouter {
    override fun routeResume(resumeId: String?) {
        TODO()
    }

    override fun init(context: Context?) {
        TODO("Not yet implemented")
    }
}

