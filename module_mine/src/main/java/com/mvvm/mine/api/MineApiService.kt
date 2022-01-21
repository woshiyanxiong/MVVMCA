package com.mvvm.mine.api

import com.common.BaseResult
import retrofit2.http.GET

/**
 * Created by yan_x
 * @date 2020/11/5/005 16:21
 * @description
 */
interface MineApiService {
    @GET("/user/lg/userinfo/json")
    suspend fun getUserInfo(): BaseResult<String>
}