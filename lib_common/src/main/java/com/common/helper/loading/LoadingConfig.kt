package com.common.helper.loading

import com.common.widget.port.LoadView

/**
 * Created by yan_x
 * @date 2021/12/15/015 11:36
 * @description
 */
object LoadingConfig {
    private var loadingView: LoadView? = null

    fun initLoadingView(load: LoadView) {
        this.loadingView = load
    }


}