package com.component.ext

import com.ca.protocol.result.Resource
import com.ca.protocol.result.handle
import com.component.uiStatus.IStatusView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun<T> Flow<Resource<T>>.loadMap(action:(Resource<T>)->Unit): Flow<Resource<T>> {
    return this.map {
        action.invoke(it)
        it
    }
}

fun<T> Flow<Resource<T>>.loadMap(iStatusView: IStatusView): Flow<Resource<T>> {
    return this.map {
        iStatusView.addResource(it)
        it
    }
}

suspend fun<T> Flow<Resource<T>>.mapSuccess(action:(T?)->Unit){
    this.collect{ data->
        data.handle(
            success = {
                action.invoke(it.data)
            }
        )
    }
}


