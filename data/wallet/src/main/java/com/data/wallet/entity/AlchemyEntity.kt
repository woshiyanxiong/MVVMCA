package com.data.wallet.entity

data class AlchemyTokenBalanceRequest(
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<Any>,
    val id: Int = 1
)

data class AlchemyTokenBalanceResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: AlchemyTokenBalanceResult?
)

data class AlchemyTokenBalanceResult(
    val address: String?,
    val tokenBalances: List<AlchemyTokenBalance>?
)

data class AlchemyTokenBalance(
    val contractAddress: String,
    val tokenBalance: String?
)

data class AlchemyTokenMetadataRequest(
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenMetadata",
    val params: List<String>,
    val id: Int = 1
)

data class AlchemyTokenMetadataResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: AlchemyTokenMetadata?
)

data class AlchemyTokenMetadata(
    val name: String?,
    val symbol: String?,
    val decimals: Int?,
    val logo: String?
)
