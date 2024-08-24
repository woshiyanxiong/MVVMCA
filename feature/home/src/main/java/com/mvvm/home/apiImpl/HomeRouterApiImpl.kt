package com.mvvm.home.apiImpl

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.ca.router_compiler.CARouterApi
import com.example.api.IHomeRouterApi

/**
 * 类的作用的简要阐述
 *
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/24/024</p>
 *
 * @author yanxiong
 */
@Route(path = "/home/server/home_api")
class HomeRouterApiImpl : IHomeRouterApi {
    override fun startTest() {
        CARouterApi.getInstance().navigation("/feature/home/details/old")
    }

    override fun init(context: Context?) {

    }
}