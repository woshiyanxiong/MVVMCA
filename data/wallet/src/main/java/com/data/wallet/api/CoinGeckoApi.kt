package com.data.wallet.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    
    @GET("https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=usd")
    suspend fun getPrice(): CoinGeckoPriceResponse
}

data class CoinGeckoPriceResponse(
    val ethereum: EthereumPriceResponse
)

data class EthereumPriceResponse(
    val usd: Double
)