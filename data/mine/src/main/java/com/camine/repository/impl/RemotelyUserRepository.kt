package com.camine.repository.impl

import com.ca.protocol.getResponse
import com.ca.protocol.result.ReSource
import com.camine.entity.LoginBeanData
import com.camine.repository.UserRepository
import com.camine.service.MineApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2023/3/11/011 15:33
 * @description
 */
internal class RemotelyUserRepository @Inject constructor(private val apiService: MineApiService) :
    UserRepository {
    override fun getUserInfo(): Flow<ReSource<String?>> {
        return getResponse {
            apiService.getUserInfo()
        }
    }

    override fun login(name: String, pwd: String): Flow<ReSource<LoginBeanData?>> {
        return getResponse {
            apiService.login(name,pwd)
        }
    }

    override fun registered(name: String, pwd: String, rePwd: String): Flow<ReSource<String?>> {
        return getResponse {
            apiService.registered(name,pwd,rePwd)
        }
    }
}