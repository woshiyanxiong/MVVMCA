package com.camine.repository.impl

import com.ca.protocol.getResponse
import com.ca.protocol.result.Resource
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
    override fun getUserInfo(): Flow<Resource<String?>> {
        return getResponse {
            apiService.getUserInfo()
        }
    }

    override fun login(name: String, pwd: String): Flow<Resource<LoginBeanData?>> {
        return getResponse {
            apiService.login(name,pwd)
        }
    }

    override fun registered(name: String, pwd: String, rePwd: String): Flow<Resource<String?>> {
        return getResponse {
            apiService.registered(name,pwd,rePwd)
        }
    }
}