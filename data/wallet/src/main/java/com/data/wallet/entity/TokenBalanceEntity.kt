package com.data.wallet.entity

/**
 * 代币余额信息
 */
data class TokenBalanceEntity(
    val contractAddress: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val balance: String,
    val logo: String? = null
)
