package com.common.helper.loading

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.common.widget.port.LoadView

/**
 * Created by yan_x
 * @date 2021/12/15/015 11:36
 * @description
 */
@Deprecated("有问题")
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
        getLoadingView()?.apply {
            setLifecycleObserver(lifecycle)
            lifecycle.lifecycle.addObserver(this)
        }

    }

    fun setHideOrShow(isShow: Boolean) {
        getLoadingView()?.setHideOrShow(isShow)
    }

    private fun getLoadingView(): LoadView? {
        Log.e("getLoadingView=","2"+(loadingView==null))
        if (loadingView==null){
            loadingView=CommonLoadingView()
        }
        return this.loadingView
    }


}