package com.common

import com.common.result.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 *create by 2020/9/10
 *@author yx
 */
class BaseResult<T> : BaseResponse<T> {
    private var errorMsg: String = ""

    @SerializedName("errorCode")
    private var code: Int = 0
    private var data: T? = null


    override fun getMsg(): String {
        return errorMsg
    }

    override fun getCode(): Int {
        return code
    }

    override fun getRequestOk(): Boolean {
        return code == 0
    }

    override fun getData(): T? {
        return data
    }
}