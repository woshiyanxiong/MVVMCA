package com.common.viewmodel

/**
 * Created by yan_x
 * @date 2021/12/16/016 14:17
 * @description loading状态
 */
data class LoadingEvent(
    val loading: Boolean,
    val loadingText: String = "加载中"
)