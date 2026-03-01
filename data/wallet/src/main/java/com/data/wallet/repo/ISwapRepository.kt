package com.data.wallet.repo

import com.data.wallet.model.SwapQuote
import com.data.wallet.model.TokenBalance
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

/**
 * 代币兑换仓库接口
 * 负责处理 DEX 交易、ERC20 代币操作等
 */
interface ISwapRepository {
    
    /**
     * 获取 ERC20 代币余额
     * @param tokenAddress 代币合约地址
     * @param walletAddress 钱包地址
     * @return 代币余额信息
     */
    fun getTokenBalance(tokenAddress: String, walletAddress: String): Flow<TokenBalance?>
    
    /**
     * 获取多个代币的余额
     * @param tokenAddresses 代币合约地址列表
     * @param walletAddress 钱包地址
     * @return 代币余额列表
     */
    fun getTokenBalances(tokenAddresses: List<String>, walletAddress: String): Flow<List<TokenBalance>>
    
    /**
     * 获取 DEX 兑换报价
     * @param fromToken 支付代币地址（0x0 表示 ETH）
     * @param toToken 接收代币地址
     * @param amount 支付金额
     * @return 兑换报价信息
     */
    fun getSwapQuote(fromToken: String, toToken: String, amount: String): Flow<SwapQuote?>
    
    /**
     * 检查代币授权额度
     * @param tokenAddress 代币合约地址
     * @param ownerAddress 代币持有者地址
     * @param spenderAddress 被授权的合约地址
     * @return 已授权的额度
     */
    fun getAllowance(
        tokenAddress: String,
        ownerAddress: String,
        spenderAddress: String
    ): Flow<BigInteger?>
    
    /**
     * 授权 ERC20 代币给指定合约
     * @param tokenAddress 代币合约地址
     * @param spenderAddress 被授权的合约地址（通常是 DEX Router）
     * @param amount 授权金额
     * @param password 钱包密码
     * @return 交易哈希，null表示失败
     */
    fun approveToken(
        tokenAddress: String,
        spenderAddress: String,
        amount: String,
        password: String
    ): Flow<String?>
    
    /**
     * 执行代币兑换（通过 DEX）
     * @param fromToken 支付代币合约地址（0x0 表示 ETH）
     * @param toToken 接收代币合约地址
     * @param fromAmount 支付金额
     * @param minToAmount 最少接收金额（滑点保护）
     * @param password 钱包密码
     * @param deadline 交易截止时间（Unix 时间戳，秒）
     * @return 交易哈希，null表示失败
     */
    fun executeSwap(
        fromToken: String,
        toToken: String,
        fromAmount: String,
        minToAmount: String,
        password: String,
        deadline: Long? = null
    ): Flow<String?>
    
    /**
     * 估算兑换交易的 Gas 费用
     * @param fromToken 支付代币地址
     * @param toToken 接收代币地址
     * @param amount 支付金额
     * @return Gas 费用（ETH）
     */
    fun estimateSwapGas(
        fromToken: String,
        toToken: String,
        amount: String
    ): Flow<String?>
    
    /**
     * 获取代币信息
     * @param tokenAddress 代币合约地址
     * @return 代币信息（名称、符号、精度等）
     */
    fun getTokenInfo(tokenAddress: String): Flow<TokenInfo?>
    
    /**
     * 获取支持的代币列表
     * @return 常用代币列表
     */
    fun getSupportedTokens(): Flow<List<TokenInfo>>
}

/**
 * 代币余额信息
 */
data class TokenBalance(
    val tokenAddress: String,
    val balance: BigInteger,
    val decimals: Int,
    val formattedBalance: String
)

/**
 * 代币信息
 */
data class TokenInfo(
    val address: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val logoUrl: String? = null
)

/**
 * 兑换报价信息
 */
data class SwapQuote(
    val fromToken: String,
    val toToken: String,
    val fromAmount: String,
    val toAmount: String,
    val exchangeRate: String,
    val priceImpact: String,
    val minimumReceived: String,
    val path: List<String>,
    val estimatedGas: String
)
