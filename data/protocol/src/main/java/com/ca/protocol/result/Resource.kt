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

package com.ca.protocol.result

import androidx.lifecycle.MutableLiveData
import com.ca.protocol.result.Resource.Success

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Resource<out R> {

    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(
        val code: Int = 200,
        val msg: String = "",
        val exception: Exception? = null
    ) :
        Resource<Nothing>()

    object Loading : Resource<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * [Success.data] if [Resource] is of type [Success]
 */
fun <T> Resource<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

val <T> Resource<T>.data: T?
    get() = (this as? Success)?.data

/**
 * Updates value of [liveData] if [Resource] is of type [Success]
 */
inline fun <reified T> Resource<T>.updateOnSuccess(liveData: MutableLiveData<T>) {
    if (this is Success) {
        liveData.value = data
    }
}

fun<T> Resource<T>.handle(
    loading: ((Resource.Loading) -> Unit)? = null,
    success: ((Success<T?>) -> Unit)? = null,
    error: ((Resource.Error) -> Unit)? = null
) {
    when (this) {
        is Resource.Loading -> loading?.invoke(this)
        is Success<T> -> success?.invoke(this)
        is Resource.Error -> error?.invoke(this)
    }
}
