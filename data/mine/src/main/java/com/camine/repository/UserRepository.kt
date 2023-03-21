package com.camine.repository

import com.ca.protocol.result.ReSource
import com.camine.entity.LoginBeanData
import kotlinx.coroutines.flow.Flow

/**
 * Created by yan_x
 * @date 2023/3/11/011 15:31
 * @description
 */
interface UserRepository {
    /**
     * 获取用户信息
     * @return Flow<String>
     */
    fun getUserInfo(): Flow<ReSource<String?>>

    fun login(name: String, pwd: String): Flow<ReSource<LoginBeanData?>>

    fun registered(name: String, pwd: String, rePwd: String): Flow<ReSource<String?>>
}