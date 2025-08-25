package com.data.wallet.repo

import com.data.wallet.model.Wallet
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
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
    
    // 创建新钱包
    fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?>
    
    // 从助记词导入钱包
    fun importWalletFromMnemonic(request: ImportWalletRequest): Flow<CreateWalletResult>


    fun getBalance(address: String): Flow<BigInteger?>
    
    // 生成助记词
    fun generateMnemonic(): Flow<List<String>>
}