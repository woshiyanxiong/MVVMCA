package com.common.helper.loading

import androidx.lifecycle.LifecycleOwner
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

    fun loadingShow() {
        getLoadingView()?.loadingShow()
    }

    fun loadingDismiss() {
        getLoadingView()?.loadingDismiss()
    }

    fun setLifecycleObserver(lifecycle: LifecycleOwner) {
        getLoadingView()?.setLifecycleObserver(lifecycle)
    }

    fun setHideOrShow(isShow: Boolean) {
        getLoadingView()?.setHideOrShow(isShow)
    }

    private fun getLoadingView(): LoadView? {
        if (loadingView==null){
            loadingView=CommonLoadingView()
        }
        return this.loadingView
    }


}