package com.data.wallet.entity

/**
 * Uniswap Token List 响应
 */
data class UniswapTokenListResponse(
    val name: String?,
    val tokens: List<UniswapToken>?
)

/**
 * Uniswap 代币信息
 * @param chainId 链ID (1=以太坊主网)
 * @param address 合约地址
 * @param name 代币名称
 * @param symbol 代币符号
 * @param decimals 精度
 * @param logoURI 图标URL
 */
data class UniswapToken(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String?
)
