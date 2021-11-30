package com.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.common.network.throwe.BaseResponseThrowable

/**
 * 状态view的扩展可自行实现
 */
class StateView {
    val isEmpty = MutableLiveData<Boolean>() //空状态
    val isErr = MutableLiveData<BaseResponseThrowable>()//错误状态
    val isContent = MutableLiveData<Boolean>()//加载视图
    val isLoading = MutableLiveData<Boolean>()//loading
}
