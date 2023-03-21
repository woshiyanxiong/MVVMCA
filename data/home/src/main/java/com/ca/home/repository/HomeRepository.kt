package com.ca.home.repository

import com.ca.home.entity.HomeResponse
import com.ca.protocol.result.ReSource
import kotlinx.coroutines.flow.Flow

/**
 * Created by yan_x
 * @date 2023/3/11/011 16:12
 * @description
 */
interface HomeRepository {
    fun getHomeInfoList(page:Int): Flow<ReSource<HomeResponse?>>
}