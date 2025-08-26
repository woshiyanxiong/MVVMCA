package com.data.wallet.model

import java.math.BigDecimal
import java.math.BigInteger

// 钱包信息
data class Wallet(
    val address: String,
    val name: String,
    val balance: BigDecimal = BigDecimal.ZERO,
    val isImported: Boolean = false
)

// 代币信息
data class Token(
    val symbol: String,
    val name: String,
    val contractAddress: String?,
    val decimals: Int,
    val balance: BigDecimal,
    val price: BigDecimal? = null
)


enum class TransactionStatus {
    PENDING, SUCCESS, FAILED
}

// 网络配置
data class Network(
    val name: String,
    val rpcUrl: String,
    val chainId: Long,
    val symbol: String,
    val explorerUrl: String
)