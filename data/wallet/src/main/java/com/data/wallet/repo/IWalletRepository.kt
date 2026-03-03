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

    /**
     * 获取代币的 USD 价格
     * @param contractAddresses 代币合约地址列表
     * @return Map<合约地址, USD价格>
     */
    fun getTokenPrices(contractAddresses: List<String>): Flow<Map<String, Double>>

    /**
     * 获取 ETH 的 USD 价格（通过 Alchemy）
     */
    fun getEthPriceFromAlchemy(): Flow<Double>
}