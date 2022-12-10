package com.component.uiStatus.impl

import androidx.lifecycle.LifecycleOwner
import com.component.uiStatus.IStatusView
import com.component.uiStatus.IUiLoadStatus
import com.component.uiStatus.UiLoadStatusManager
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2022/12/9/009 16:34
 * @description
 */
class IUiLoadStatusImpl @Inject constructor() : IUiLoadStatus {

    override fun initUiStatus(owe: LifecycleOwner?, vararg statusView: IStatusView) {
        owe?:return
        UiLoadStatusManager(owe, *statusView)
    }
}