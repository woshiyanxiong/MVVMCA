package com.data.wallet.entity

/**
 * Alchemy API 实体类
 * 
 * 本文件包含与 Alchemy JSON-RPC API 交互所需的所有请求和响应数据类。
 * Alchemy 提供增强的以太坊 API，支持代币余额查询、元数据获取和价格查询等功能。
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

// ==================== 代币价格相关 ====================

/**
 * 获取代币价格的请求体
 * 
 * @property jsonrpc JSON-RPC 版本，固定为 "2.0"
 * @property method API 方法名，固定为 "alchemy_getTokenPrices"
 * @property params 参数列表，包含要查询的代币地址信息
 * @property id 请求 ID
 */
data class AlchemyTokenPriceRequest(
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenPrices",
    val params: List<AlchemyTokenPriceParams>,
    val id: Int = 1
)

/**
 * 代币价格查询参数
 * 
 * @property addresses 要查询价格的代币地址列表
 */
data class AlchemyTokenPriceParams(
    val addresses: List<AlchemyTokenAddress>
)

/**
 * 代币地址信息
 * 
 * @property network 网络标识，如 "eth-mainnet"
 * @property address 代币合约地址，ETH 原生代币使用 "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
 */
data class AlchemyTokenAddress(
    val network: String = "eth-mainnet",
    val address: String
)

/**
 * 代币价格响应体
 */
data class AlchemyTokenPriceResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: AlchemyTokenPriceResult?
)

/**
 * 代币价格查询结果
 * 
 * @property data 代币价格数据列表
 */
data class AlchemyTokenPriceResult(
    val data: List<AlchemyTokenPriceData>?
)

/**
 * 单个代币的价格数据
 * 
 * @property network 网络标识
 * @property address 代币合约地址
 * @property prices 价格信息列表 (支持多种货币)
 */
data class AlchemyTokenPriceData(
    val network: String?,
    val address: String?,
    val prices: List<AlchemyPrice>?
)

/**
 * 价格信息
 * 
 * @property currency 货币类型，如 "usd"
 * @property value 价格值
 * @property lastUpdatedAt 最后更新时间 (ISO 8601 格式)
 */
data class AlchemyPrice(
    val currency: String?,
    val value: String?,
    val lastUpdatedAt: String?
)
