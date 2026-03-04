package com.data.wallet.entity

/**
 * Alchemy API 实体类
 * 
 * 本文件包含与 Alchemy JSON-RPC API 交互所需的所有请求和响应数据类。
 * Alchemy 提供增强的以太坊 API，支持代币余额查询和元数据获取等功能。
 */

// ==================== 代币余额相关 ====================

/**
 * 获取代币余额的请求体
 * 
 * @property jsonrpc JSON-RPC 版本，固定为 "2.0"
 * @property method API 方法名，固定为 "alchemy_getTokenBalances"
 * @property params 参数列表: [钱包地址, 代币类型("erc20")]
 * @property id 请求 ID，用于匹配响应
 */
data class AlchemyTokenBalanceRequest(
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<Any>,
    val id: Int = 1
)

/**
 * 代币余额响应体
 * 
 * @property jsonrpc JSON-RPC 版本
 * @property id 请求 ID
 * @property result 查询结果，包含地址和代币余额列表
 */
data class AlchemyTokenBalanceResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: AlchemyTokenBalanceResult?
)

/**
 * 代币余额查询结果
 * 
 * @property address 查询的钱包地址
 * @property tokenBalances 该地址持有的所有代币余额列表
 */
data class AlchemyTokenBalanceResult(
    val address: String?,
    val tokenBalances: List<AlchemyTokenBalance>?
)

/**
 * 单个代币的余额信息
 * 
 * @property contractAddress 代币合约地址
 * @property tokenBalance 代币余额 (十六进制格式，如 "0x1234")，需要根据 decimals 转换
 */
data class AlchemyTokenBalance(
    val contractAddress: String,
    val tokenBalance: String?
)

// ==================== 代币元数据相关 ====================

/**
 * 获取代币元数据的请求体
 * 
 * @property jsonrpc JSON-RPC 版本，固定为 "2.0"
 * @property method API 方法名，固定为 "alchemy_getTokenMetadata"
 * @property params 参数列表: [代币合约地址]
 * @property id 请求 ID
 */
data class AlchemyTokenMetadataRequest(
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenMetadata",
    val params: List<String>,
    val id: Int = 1
)

/**
 * 代币元数据响应体
 */
data class AlchemyTokenMetadataResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: AlchemyTokenMetadata?
)

/**
 * 代币元数据信息
 * 
 * @property name 代币名称，如 "Tether USD"
 * @property symbol 代币符号，如 "USDT"
 * @property decimals 代币精度，如 18 表示余额需要除以 10^18
 * @property logo 代币 Logo 图片 URL
 */
data class AlchemyTokenMetadata(
    val name: String?,
    val symbol: String?,
    val decimals: Int?,
    val logo: String?
)
