package com.ca.protocol

import com.ca.protocol.result.BaseResponse
import com.ca.protocol.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun <Response, T : BaseResponse<Response>> getResponse(fetchData: suspend () -> T): Flow<Resource<Response?>> {
    return flow {
        try {
            emit(Resource.Loading)
            val data = fetchData()
            if (data.getRequestOk()) {
                emit(Resource.Success(data.getData()))
            } else {
                emit(Resource.Error(code = data.getCode(), msg = data.getMsg()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)
}