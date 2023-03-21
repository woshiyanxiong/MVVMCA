package com.ca.protocol

import com.ca.protocol.result.BaseResponse
import com.ca.protocol.result.ReSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun <Response, T : BaseResponse<Response>> getResponse(fetchData: suspend () -> T): Flow<ReSource<Response?>> {
    return flow {
        try {
            emit(ReSource.Loading)
            val data = fetchData()
            if (data.getRequestOk()) {
                emit(ReSource.Success(data.getData()))
            } else {
                emit(ReSource.Error(code = data.getCode(), msg = data.getMsg()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ReSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)
}