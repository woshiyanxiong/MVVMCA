package com.component.uiStatus

import com.component.result.ReSource
import kotlinx.coroutines.flow.Flow

/**
 * Created by yan_x
 * @date 2022/12/9/009 10:12
 * @description
 */
interface IUiStatusResource {
    fun loadingStatus(): Flow<Boolean>
    fun errorStatus():Flow<ReSource<ReSource.Error>>
}