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
     */
    @GET
    suspend fun getTokenList(@Url url: String = "https://tokens.uniswap.org/"): UniswapTokenListResponse?
}
