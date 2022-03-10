package com.common.ext

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.network.RequestObserver
import com.common.network.throwe.BaseResponseThrowable
import com.common.result.BaseResponse
import com.common.result.ReSource
import com.common.result.handle
import com.common.viewmodel.StateView
import com.uber.autodispose.SingleSubscribeProxy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.annotations.SchedulerSupport
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.ObjectHelper
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


/**
 *create by 2019/12/30
 * 扩展函数基类
 *@author yx
 */
fun Activity.toast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (TextUtils.isEmpty(msg)) {
        return
    }
    Toast.makeText(this, msg, duration).show()
}

fun Activity.toast(msg: Int?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg!!, duration).show()
}

fun <T> Single<T>.single(): Single<T> =
    this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

//fun<T> AutoDisposeConverter<T>.ad(pr: ScopeProvider): AutoDisposeConverter<T>? = AutoDispose.autoDisposable<T>(pr)


@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun <T> Single<T>.subscribes(
    onSuccess: (T) -> Unit,
    onError: (BaseResponseThrowable) -> Unit
): Disposable {
    ObjectHelper.requireNonNull(onSuccess, "onSuccess is null")
    ObjectHelper.requireNonNull(onError, "onError is null")
    val observer: RequestObserver<T> = RequestObserver(onSuccess, onError)
    subscribe(observer)
    return observer
}

fun <T> SingleSubscribeProxy<T>.subscribes(
    onSuccess: (T) -> Unit,
    onError: (BaseResponseThrowable) -> Unit
) {
    ObjectHelper.requireNonNull(onSuccess, "onSuccess is null")
    ObjectHelper.requireNonNull(onError, "onError is null")
    val observer: RequestObserver<T> = RequestObserver(onSuccess, onError)
    subscribe(observer)
}

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

fun <T> ViewModel.launch(
    block: suspend CoroutineScope.() -> Flow<ReSource<T>>,
    success:(ReSource<T>) -> Unit,
    stateView: StateView?=null
) {
    viewModelScope.launch {
        val data = block()
        data.collect {
            it.handle(
                loading = {
                    Log.e("loading", "loading")
                    stateView?.isLoading?.value = true
                },
                success = {
                    Log.e("success", "success")
                    stateView?.isLoading?.value = false
                },
                error = { error->
                    Log.e("error", "error=${error.msg}")
                    stateView?.isLoading?.value = false
                },
            )
            success(it)
        }

    }
}





