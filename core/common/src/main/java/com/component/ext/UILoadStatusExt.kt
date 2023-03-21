package com.component.ext

import com.ca.protocol.result.ReSource
import com.ca.protocol.result.handle
import com.component.uiStatus.IStatusView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun<T> Flow<ReSource<T>>.loadMap(action:(ReSource<T>)->Unit): Flow<ReSource<T>> {
    return this.map {
        action.invoke(it)
        it
    }
}

fun<T> Flow<ReSource<T>>.loadMap(iStatusView: IStatusView): Flow<ReSource<T>> {
    return this.map {
        iStatusView.addResource(it)
        it
    }
}

suspend fun<T> Flow<ReSource<T>>.mapSuccess(action:(T?)->Unit){
    this.collect{ data->
        data.handle(
            success = {
                action.invoke(it.data)
            }
        )
    }
}


