package com.mvvm.mine.repository

import com.mvvm.mine.api.MineApiService
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2020/11/19/019 16:22
 * @description
 */
class MineRepository @Inject constructor(private val api: MineApiService) {
    suspend fun getUserInfo() = api.getUserInfo()
}