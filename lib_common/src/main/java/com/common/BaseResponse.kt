package com.common

import com.google.gson.annotations.SerializedName

/**
 *create by 2020/9/10
 *@author yx
 */
open class BaseResponse<T>(
    val errorMsg: String,
    @SerializedName("errorCode")
    val code: Int,
    val data: T
)