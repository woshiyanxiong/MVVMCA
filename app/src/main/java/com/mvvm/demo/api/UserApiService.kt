package com.mvvm.demo.api

import com.common.BaseResult
import com.common.LoginBeanData
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:28
 * @description
 */

interface UserApiService {

    @POST("/user/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): BaseResult<LoginBeanData>

    @POST("/user/register")
    suspend fun registered(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("repassword") repassword: String
    ): BaseResult<String>


}