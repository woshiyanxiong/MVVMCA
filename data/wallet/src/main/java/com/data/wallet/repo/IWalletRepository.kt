package com.data.wallet.repo

import com.data.wallet.entity.MainWalletInfoEntity
import com.data.wallet.model.Wallet
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.model.TransactionModel
import com.data.wallet.entity.TokenBalanceEntity
import org.web3j.crypto.Credentials
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

data class CreateWalletResult(
    val mnemonic: List<String>,
    val walletAddress: String,
    val walletFileName: String
)

/**
 * 兑换页面聚合数据
 */
data class SwapTokenData(
    val ethPrice: Double,
    val balanceMap: Map<String, String>,
    val tokenList: List<com.data.wallet.entity.UniswapToken>
)

interface IWalletRepository {
    fun getWalletList(): Flow<List<String>>

    /**
     * 发送转账交易
     * @param toAddress 收款地址
     * @param amount 转账金额（ETH）
     * @param password 钱包密码
     * @return 交易哈希，null表示失败
     */
    fun sendTransaction(toAddress: String, amount: String, password: String): Flow<String?>

    /**
     * 获取当前信息
     */
    fun getMainWalletInfo():Flow<MainWalletInfoEntity?>
    
    // 创建新钱包
    fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?>
    
    // 从助记词导入钱包
    fun importWalletFromMnemonic(request: ImportWalletRequest): Flow<CreateWalletResult>


    fun getBalance(address: String): Flow<BigInteger?>
    
    // 生成助记词
    fun generateMnemonic(): Flow<List<String>>

    fun getTransactions(address: String, limit: Int = 10): Flow<List<TransactionModel>>
    
    // 获取ETH价格
    fun getEthPrice(): Flow<Double?>

    // 获取地址下所有代币余额（通过 Alchemy）
    fun getTokenBalances(address: String): Flow<List<TokenBalanceEntity>>

    // 获取当前钱包所有代币余额（ETH + ERC20），通过合约地址做 key
    fun getWalletBalanceMap(): Flow<Map<String, String>>

    // 获取 Uniswap 主网热门代币列表
    fun getUniswapTokenList(): Flow<List<com.data.wallet.entity.UniswapToken>>

    // 兑换页面聚合数据：代币列表 + 余额 + ETH价格，一个出口
    fun getSwapTokenData(): Flow<SwapTokenData>

    /**
     * 获取兑换报价信息（Uniswap V2 报价 + Gas 预估）
     * @param fromToken 支付代币地址
     * @param toToken 接收代币地址
     * @param amountIn 输入金额字符串（人类可读，如 "0.1"）
     * @param fromDecimals 支付代币精度
     * @param toDecimals 接收代币精度
     */
    fun getSwapQuote(
        fromToken: String,
        toToken: String,
        amountIn: String,
        fromDecimals: Int,
        toDecimals: Int
    ): Flow<SwapQuoteResult>

    /**
     * 执行兑换交易（加载凭证 → 检查/执行授权 → 执行 Swap）
     * @param password 钱包密码
     * @param fromToken 支付代币地址
     * @param toToken 接收代币地址
     * @param amountIn 输入金额字符串（人类可读）
     * @param fromDecimals 支付代币精度
     * @param amountOutMinWei 最小接收金额 (Wei)
     * @param gasPrice Gas 价格 (Wei)
     * @param gasLimit Gas 限制
     * @return 交易哈希，null 表示失败
     */
    fun executeSwap(
        password: String,
        fromToken: String,
        toToken: String,
        amountIn: String,
        fromDecimals: Int,
        amountOutMinWei: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger
    ): Flow<String?>
}

/**
 * 兑换报价结果
 */
data class SwapQuoteResult(
    val amountOut: String = "0",
    val exchangeRate: String = "0",
    val gasFeeEth: String = "0",
    val gasLimit: java.math.BigInteger = java.math.BigInteger.ZERO,
    val gasPrice: java.math.BigInteger = java.math.BigInteger.ZERO,
    val priceImpact: String = "< 0.01%",
    val minimumReceived: String = "0",
    val error: String? = null
)