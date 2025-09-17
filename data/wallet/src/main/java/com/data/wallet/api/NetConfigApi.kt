package com.data.wallet.api

import com.data.wallet.model.NetworkConfigData
import com.data.wallet.model.NetworkInfo
import retrofit2.http.GET

/**
 *create by 2025/9/7
 *@author yx
 */
interface NetConfigApi {
    @GET("https://config-inky.vercel.app/wallet/api/api.json")
    suspend fun getNetConfig(): NetworkConfigData
}