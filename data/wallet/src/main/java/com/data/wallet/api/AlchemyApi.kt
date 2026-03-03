package com.data.wallet.api

import com.data.wallet.entity.AlchemyTokenBalanceRequest
import com.data.wallet.entity.AlchemyTokenBalanceResponse
import com.data.wallet.entity.AlchemyTokenMetadataRequest
import com.data.wallet.entity.AlchemyTokenMetadataResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface AlchemyApi {


    @POST
    suspend fun getTokenBalances(
        @Url url: String,
        @Body request: AlchemyTokenBalanceRequest
    ): AlchemyTokenBalanceResponse?

    @POST
    suspend fun getTokenMetadata(
        @Url url: String,
        @Body request: AlchemyTokenMetadataRequest
    ): AlchemyTokenMetadataResponse?
}
