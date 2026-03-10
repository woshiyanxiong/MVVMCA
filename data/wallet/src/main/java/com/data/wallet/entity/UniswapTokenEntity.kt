package com.data.wallet.entity

/**
 * Uniswap Token List 响应 (对应 uniswap_tokens.json)
 */
data class UniswapTokenListResponse(
    val name: String?,
    val timestamp: String?,
    val version: UniswapTokenVersion?,
    val logoURI: String?,
    val keywords: List<String>?,
    val tokens: List<UniswapToken>?
)

data class UniswapTokenVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
)

/**
 * Uniswap 代币信息
 */
data class UniswapToken(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String?,
    val extensions: Map<String, Any>? = null
)
