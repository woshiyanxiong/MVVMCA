package com.data.wallet.model

/**
 * 网络配置数据类
 */
data class NetworkConfigData(
    val networks: List<NetworkInfo>,
    val tokens: List<TokenInfo>
)

/**
 * 网络信息
 */
data class NetworkInfo(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val apiKey: String,
    val chainId: Int,
    val currencySymbol: String,
    val blockExplorerUrl: String
)

/**
 * 代币信息
 */
data class TokenInfo(
    val name: String,
    val symbol: String,
    val contractAddress: String,
    val decimals: Int,
    val chainId: Int
)