package com.data.wallet.api

import com.data.wallet.entity.UniswapTokenListResponse
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Uniswap Token List API
 * 从 https://tokens.uniswap.org/ 获取代币列表
 */
interface UniswapTokenApi {

    /**
     * 获取 Uniswap 默认代币列表
     * @param url 完整的 API 地址
     */
    @GET
    suspend fun getTokenList(@Url url: String): UniswapTokenListResponse?
}
