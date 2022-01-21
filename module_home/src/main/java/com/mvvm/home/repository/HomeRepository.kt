package com.mvvm.home.repository


import com.common.ext.getResponse
import com.mvvm.home.api.HomeApiService
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 9:45
 * @description
 */
class HomeRepository @Inject constructor(private val api: HomeApiService) {
    suspend fun getHomeInfoList(pageNum: Int) = getResponse {
        api.getInfoList(pageNum)
    }
}