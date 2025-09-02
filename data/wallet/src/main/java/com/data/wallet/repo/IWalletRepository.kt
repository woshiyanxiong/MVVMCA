package com.data.wallet.repo

import com.data.wallet.entity.MainWalletInfoEntity
import com.data.wallet.model.Wallet
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.model.TransactionModel
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

//    fun sendTransaction()

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
}