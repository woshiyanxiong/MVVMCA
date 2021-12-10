package com.mvvm.demo

import com.mvvm.demo.api.UserApiService
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/10/010 14:55
 * @description
 */
class UserRepository @Inject constructor(private val api: UserApiService) {

    suspend fun login(name:String,pwd:String)=api.login(name,pwd)

    suspend fun registered(name:String,pwd:String,rePwd:String)=api.registered(name,pwd,rePwd)

}