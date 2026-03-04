package com.data.wallet.api

import com.data.wallet.entity.AlchemyTokenBalanceRequest
import com.data.wallet.entity.AlchemyTokenBalanceResponse
import com.data.wallet.entity.AlchemyTokenMetadataRequest
import com.data.wallet.entity.AlchemyTokenMetadataResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Alchemy API 接口
 * 
 * Alchemy 是一个区块链开发平台，提供增强的以太坊 API 服务。
 * 本接口封装了 Alchemy 的 JSON-RPC 方法，用于查询代币余额和元数据信息。
 * 
 * API 文档: https://docs.alchemy.com/reference/api-overview
 * 
 * 使用方式:
 * - 所有请求都是 POST 方法，发送 JSON-RPC 格式的请求体
 * - URL 格式: https://eth-mainnet.g.alchemy.com/v2/{API_KEY}
 * - 请求体包含 jsonrpc、method、params、id 字段
 */
interface AlchemyApi {

    /**
     * 获取指定地址的所有 ERC20 代币余额
     * 
     * @param url Alchemy API 完整 URL (包含 API Key)
     * @param request 请求体，包含钱包地址和代币类型 ("erc20")
     * @return 代币余额列表，包含合约地址和原始余额 (十六进制格式)
     * 
     * 示例请求:
     * {
     *   "jsonrpc": "2.0",
     *   "method": "alchemy_getTokenBalances",
     *   "params": ["0x地址", "erc20"],
     *   "id": 1
     * }
     */
    @POST
    suspend fun getTokenBalances(
        @Url url: String,
        @Body request: AlchemyTokenBalanceRequest
    ): AlchemyTokenBalanceResponse?

    /**
     * 获取指定代币合约的元数据信息
     * 
     * @param url Alchemy API 完整 URL (包含 API Key)
     * @param request 请求体，包含代币合约地址
     * @return 代币元数据，包含名称、符号、精度、Logo 等信息
     * 
     * 示例请求:
     * {
     *   "jsonrpc": "2.0",
     *   "method": "alchemy_getTokenMetadata",
     *   "params": ["0x代币合约地址"],
     *   "id": 1
     * }
     */
    @POST
    suspend fun getTokenMetadata(
        @Url url: String,
        @Body request: AlchemyTokenMetadataRequest
    ): AlchemyTokenMetadataResponse?
}
