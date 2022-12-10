package com.component.uiStatus

import com.component.result.ReSource

/**
 * Created by yan_x
 * @date 2022/12/9/009 10:33
 * @description
 */
interface IStatusView:IUiStatusResource {
    fun<T> addResource(r:ReSource<T>)
}