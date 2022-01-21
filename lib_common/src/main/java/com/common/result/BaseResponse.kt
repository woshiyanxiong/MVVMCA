package com.common.result

/**
 * Created by yan_x
 * @date 2022/1/17/017 13:41
 * @description
 */
interface BaseResponse<T> {

    /**
     * 获取msg
     * @return String
     */
    fun getMsg(): String

    /**
     * 获取code
     * @return String
     */
    fun getCode(): Int

    /**
     * 判断是否成功
     * @return Boolean
     */
    fun getRequestOk(): Boolean

    fun getData():T?
}