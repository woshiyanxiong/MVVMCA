package com.common.viewmodel

import androidx.lifecycle.viewModelScope
import com.common.BaseResult
import com.common.network.throwe.BaseResponseThrowable
import com.common.network.throwe.ThrowableHandler
import kotlinx.coroutines.*

/**
 *create by 2020/5/22
 * ViewModel 基础类
 *@author yx
 */
open class BaseViewModel : BaseLifeViewModel() {

    var stateView = StateView()

    private fun launchUi(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch { block() }

    //过滤结果
    fun <T> asyncExecute(
        request: suspend CoroutineScope.() -> BaseResult<T>,
        success: (T) -> Unit,
        error: suspend CoroutineScope.(BaseResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit = {}
    ) {
        launchUi {
            handleRequest(withContext(Dispatchers.IO) {
                request
            }, {response->
                executeResponse(response){
                    success(it)
                }
            }, {
                error(it)
            }, {
                complete()
            })
        }
    }

    /**
     * 带loading的请求
     */
    fun <T> async(
        request: suspend CoroutineScope.() -> T,
        success: (T) -> Unit={},
        showDialog: Boolean = true,
        error: suspend CoroutineScope.(BaseResponseThrowable) -> Unit={},
        complete: suspend CoroutineScope.() -> Unit = {
            if (showDialog) {
                stateView.isLoading.value = false
            }
        }

    ) {
        if (showDialog) {
            stateView.isLoading.value = true
        }
        launchUi {
            handleRequest(withContext(Dispatchers.IO) {
                request
            }, {
                success(it)
            }, {
                error(it)
                stateView.isErr.value=it
            }, {
                complete()
                if (showDialog) {
                    stateView.isLoading.value = false
                }
            })
        }
    }

    private suspend fun <T> handleRequest(
        block: suspend CoroutineScope.() -> T,
        success: suspend CoroutineScope.(T) -> Unit,
        error: suspend CoroutineScope.(BaseResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                success(block())
            } catch (e: java.lang.Exception) {
                error(ThrowableHandler.handleThrowable(e))
            } finally {
                complete()
            }
        }
    }

    //过滤返回数据
    private suspend fun <T> executeResponse(
        response: BaseResult<T>,
        success: suspend CoroutineScope.(T) -> Unit
    ) {
        coroutineScope {
            if (response.getCode() == 200) response.getData()?.let { success(it) }
            else throw BaseResponseThrowable(response.getCode(), response.getMsg())
        }
    }
}

