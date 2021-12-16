package com.common.widget.port

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by yan_x
 * @date 2021/12/15/015 10:51
 * @description loadingView
 */
abstract class LoadView : LifecycleObserver {

    /**
     * 加载中
     */
    abstract fun loadingShow()

    /**
     * 取消加载
     */
    abstract fun loadingDismiss()

    /**
     * 控制生命周期
     * @param lifecycle LifecycleOwner
     */
    abstract fun setLifecycleObserver(lifecycle: LifecycleOwner)

    /**
     * 控制加载获取取消
     * @param isShow Boolean true加载 false取消加载
     */
    abstract fun setHideOrShow(isShow: Boolean)

    /**
     * 注销
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {

    }
}