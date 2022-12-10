package com.component.uiStatus

import androidx.lifecycle.LifecycleOwner

/**
 * Created by yan_x
 * @date 2022/12/9/009 16:32
 * @description
 */
interface IUiLoadStatus {
    fun initUiStatus(owe: LifecycleOwner?, vararg statusView: IStatusView)
}