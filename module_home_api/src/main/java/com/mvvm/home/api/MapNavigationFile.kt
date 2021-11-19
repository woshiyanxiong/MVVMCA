package com.mvvm.home.api

import android.app.Activity
import android.view.View

/**
 * Created by yan_x
 * @date 2021/11/18/018 14:51
 * @description kt api化的需要以文件的形式，这样才不会爆红
 */
interface MapNavigation {
    fun createMap(content: Activity,onClick:(Int)->Unit={})
    fun show(view: View)
    fun disses()
}