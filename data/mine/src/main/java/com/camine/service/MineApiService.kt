package com.camine.service

import com.ca.protocol.BaseResult
import com.camine.entity.LoginBeanData
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by yan_x
 * @date 2023/3/11/011 15:08
 * @description
 */
interface MineApiService {
    @GET("/user/lg/userinfo/json")
    suspend fun getUserInfo(): BaseResult<String>

    @POST("/user/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): BaseResult<LoginBeanData>

    @POST("/user/register")
    suspend fun registered(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("repassword") repassword: String,
    ): BaseResult<String>
}