package com.mvvm.home.widget

import android.app.Activity
import android.content.Context
import android.view.View
import com.mvvm.home.api.MapNavigation


import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/18/018 10:40
 * @description
 */
class MapBuilderImpl @Inject constructor()  : MapNavigation {
    private var mapWindows: MapWindows? = null
    override fun createMap(content: Activity,onClick:(Int)->Unit) {
        mapWindows = MapWindows(content).apply {
            this.onClick=onClick
        }
    }

    override fun show(view: View) {
        mapWindows?.showView(view)
    }

    override fun disses() {
        mapWindows?.dismiss()
    }

}