package com.component.ext

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import com.component.result.BaseResponse
import com.component.result.ReSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


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







