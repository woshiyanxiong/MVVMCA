/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.common.result

import androidx.lifecycle.MutableLiveData
import com.common.result.ReSource.Success

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ReSource<out R> {

    data class Success<out T>(val data: T) : ReSource<T>()
    data class Error(
        val code: Int = 200,
        val msg: String = "",
        val exception: Exception? = null
    ) :
        ReSource<Nothing>()

    object Loading : ReSource<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * [Success.data] if [ReSource] is of type [Success]
 */
fun <T> ReSource<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

val <T> ReSource<T>.data: T?
    get() = (this as? Success)?.data

/**
 * Updates value of [liveData] if [ReSource] is of type [Success]
 */
inline fun <reified T> ReSource<T>.updateOnSuccess(liveData: MutableLiveData<T>) {
    if (this is Success) {
        liveData.value = data
    }
}

fun ReSource<*>.handle(
    loading: ((ReSource.Loading) -> Unit)? = null,
    success: ((Success<*>) -> Unit)? = null,
    error: ((ReSource.Error) -> Unit)? = null
) {
    when (this) {
        is ReSource.Loading -> loading?.invoke(this)
        is Success<*> -> success?.invoke(this)
        is ReSource.Error -> error?.invoke(this)
    }
}
