package com.component.uiStatus.impl

import android.util.Log
import com.component.result.ReSource
import com.component.result.handle
import com.component.uiStatus.IStatusView
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2022/12/9/009 10:22
 * @description
 */
class IUiStatusResourceImpl @Inject constructor() : IStatusView {
    private val listResource = mutableListOf<ReSource<*>>()

    private val _loading = MutableStateFlow<Boolean?>(null)
    private val loading = _loading.map {
        it ?: false
    }

    private val errorStatus= MutableSharedFlow<ReSource.Error>(
        0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    override fun loadingStatus(): Flow<Boolean> {
        return loading
    }

    override fun errorStatus(): Flow<ReSource<ReSource.Error>> {
        return errorStatus
    }

    override fun <T> addResource(r: ReSource<T>) {
        r.handle(
            loading = {
                Log.e("loadingImpl", "loading")
                _loading.tryEmit(true)
            },
            success = {
                Log.e("successImpl", "success")
                _loading.tryEmit(false)
            },
            error = { error ->
                Log.e("errorImpl", "error=${error.msg}")
                _loading.tryEmit(false)
                errorStatus.tryEmit(error)
            },
        )
    }


}