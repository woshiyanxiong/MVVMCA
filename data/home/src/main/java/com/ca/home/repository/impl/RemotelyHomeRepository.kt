package com.ca.home.repository.impl

import com.ca.home.entity.HomeResponse
import com.ca.home.repository.HomeRepository
import com.ca.home.service.HomeApiService
import com.ca.protocol.getResponse
import com.ca.protocol.result.ReSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2023/3/11/011 16:15
 * @description
 */
internal class RemotelyHomeRepository @Inject constructor(private val apiService: HomeApiService) :
    HomeRepository {
    override fun getHomeInfoList(page: Int): Flow<ReSource<HomeResponse?>> {
        return getResponse {
            apiService.getInfoList(page)
        }
    }
}