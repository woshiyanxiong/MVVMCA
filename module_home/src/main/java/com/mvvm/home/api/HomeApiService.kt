package com.mvvm.home.api

import com.common.BaseResponse
import com.mvvm.home.bean.HomeResponse


import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by yan_x
 * @date 2021/11/5/005 18:12
 * @description
 */
interface HomeApiService {
    @GET("/article/list/{pageNum}/json")
    suspend fun getInfoList(@Path("pageNum") pageNum: Int=0): BaseResponse<HomeResponse>
}