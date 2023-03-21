package com.ca.home.service

import com.ca.home.entity.HomeResponse
import com.ca.protocol.BaseResult

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by yan_x
 * @date 2023/3/11/011 16:06
 * @description
 */
interface HomeApiService {
    @GET("/article/list/{pageNum}/json")
    suspend fun getInfoList(@Path("pageNum") pageNum: Int=0): BaseResult<HomeResponse>
}