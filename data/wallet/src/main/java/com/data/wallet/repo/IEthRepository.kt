package com.data.wallet.repo

import com.data.wallet.model.TransactionModel
import kotlinx.coroutines.flow.Flow
import org.web3j.crypto.Credentials
import java.math.BigDecimal
import java.math.BigInteger

/**
 * ETH 链上操作仓库接口
 * 负责 ETH 余额查询、交易发送、价格获取等链上操作
 */
interface IEthRepository {

    /**
     * 获取指定地址的 ETH 余额
     * @param address 钱包地址
     * @return 余额 (Wei)
     */
    fun getBalance(address: String): Flow<BigInteger?>

    /**
     * 发送 ETH 交易
     * @param credentials 钱包凭证
     * @param toAddress 收款地址
     * @param amount 转账金额 (ETH)
     * @return 交易哈希
     */
    suspend fun sendTransaction(
        credentials: Credentials,
        toAddress: String,
        amount: BigDecimal
    ): String?

    /**
     * 获取交易记录
     * @param address 钱包地址
     * @param limit 返回数量限制
     * @return 交易记录列表
     */
    fun getTransactions(address: String, limit: Int = 10): Flow<List<TransactionModel>>

    /**
     * 获取 ETH/USD 价格 (通过 Chainlink 预言机)
     * @return ETH 价格 (USD)
     */
    fun getEthPrice(): Flow<Double?>

    /**
     * 验证地址格式是否正确
     */
    fun isValidAddress(address: String): Boolean

    /**
     * 从钱包文件加载凭证
     */
    suspend fun loadCredentials(password: String, walletFilePath: String): Credentials

    /**
     * 通过 Uniswap V2 Router 查询兑换报价
     * @param amountInWei 输入金额 (Wei)
     * @param path 兑换路径 (代币地址列表)
     * @return 输出金额 (Wei)，null 表示查询失败
     */
    fun getAmountsOut(amountInWei: java.math.BigInteger, path: List<String>): Flow<java.math.BigInteger?>

    /**
     * 获取当前 Gas 价格
     * @return Gas 价格 (Wei)
     */
    fun getGasPrice(): Flow<java.math.BigInteger?>

    /**
     * 预估 Swap 交易的 Gas 用量
     * @return Gas Limit
     */
    fun estimateSwapGas(
        fromAddress: String,
        fromToken: String,
        toToken: String,
        amountInWei: java.math.BigInteger,
        amountOutMinWei: java.math.BigInteger
    ): Flow<java.math.BigInteger?>

    /**
     * 检查 ERC20 代币对 Uniswap Router 的授权额度
     * @return 已授权额度 (Wei)
     */
    fun getAllowance(tokenAddress: String, ownerAddress: String): Flow<java.math.BigInteger?>

    /**
     * 执行 ERC20 approve 授权
     * @return 交易哈希，null 表示失败
     */
    suspend fun approveToken(
        credentials: Credentials,
        tokenAddress: String,
        amount: java.math.BigInteger
    ): String?

    /**
     * 执行 Uniswap V2 Swap 交易
     * @return 交易哈希，null 表示失败
     */
    suspend fun executeSwap(
        credentials: Credentials,
        fromToken: String,
        toToken: String,
        amountInWei: java.math.BigInteger,
        amountOutMinWei: java.math.BigInteger,
        gasPrice: java.math.BigInteger,
        gasLimit: java.math.BigInteger
    ): String?
}
