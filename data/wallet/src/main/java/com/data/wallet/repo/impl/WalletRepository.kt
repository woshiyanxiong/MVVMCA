package com.data.wallet.repo.impl

import android.util.Log
import com.data.wallet.repo.IWalletRepository
import com.data.wallet.repo.CreateWalletResult
import com.data.wallet.model.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.DefaultBlockParameterName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigInteger
import java.math.BigDecimal
import java.security.SecureRandom
import javax.inject.Inject
import java.io.File
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicException
import org.bitcoinj.crypto.ChildNumber
import com.data.wallet.storage.WalletStore
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

internal class WalletRepository @Inject constructor(
    private val walletStore: WalletStore
) : IWalletRepository {
    // 使用国内可访问的节点
    private val nodeUrl = "https://eth-mainnet.nodereal.io/v1/1659dfb40aa24bbb8153a677b98064d7"

    // 创建Web3j实例
    private val web3: Web3j = Web3j.build(HttpService(nodeUrl))

    override fun getWalletList(): Flow<List<String>> {
        return walletStore.getWalletList()
    }



    // 创建新钱包
    suspend fun createWallet(password: String, walletDir: String): String = withContext(Dispatchers.IO) {
        val walletFile = WalletUtils.generateNewWalletFile(password, java.io.File(walletDir))
        walletFile
    }

    // 从私钥加载钱包
    fun loadWalletFromPrivateKey(privateKey: String): Credentials {
        return Credentials.create(privateKey)
    }

    // 从钱包文件加载
    suspend fun loadWalletFromFile(password: String, walletFilePath: String): Credentials = withContext(Dispatchers.IO) {
        WalletUtils.loadCredentials(password, walletFilePath)
    }

    // 获取当前区块号
    suspend fun getCurrentBlockNumber(): BigInteger = withContext(Dispatchers.IO) {
        web3.ethBlockNumber().send().blockNumber
    }

    // 发送交易
    suspend fun sendTransaction(
        credentials: Credentials,
        toAddress: String,
        amount: BigDecimal
    ): String = withContext(Dispatchers.IO) {
        val nonce = web3.ethGetTransactionCount(
            credentials.address, DefaultBlockParameterName.LATEST
        ).send().transactionCount
        
        val gasPrice = web3.ethGasPrice().send().gasPrice
        val gasLimit = BigInteger.valueOf(21000)
        val value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        
        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce, gasPrice, gasLimit, toAddress, value
        )
        
        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        
        web3.ethSendRawTransaction(hexValue).send().transactionHash
    }

    // 验证地址格式
    fun isValidAddress(address: String): Boolean {
        return WalletUtils.isValidAddress(address)
    }

    override fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?> = flow {
        val currentAddress = walletStore.getCurrentWalletAddress().firstOrNull()
        if (!currentAddress.isNullOrBlank()){
            val file = walletStore.getWalletFileName(currentAddress).firstOrNull() ?:""
            val entity = CreateWalletResult(
                mnemonic = ("fatal, stand, various, brisk, aisle, object, proof, skull, else, runway, jaguar, unique").split(", "),
                walletAddress = currentAddress,
                walletFileName = file
            )
            return@flow emit(entity)
        }
        // 1. 生成助记词
        val mnemonic = generateMnemonic().firstOrNull()?: emptyList()
        Log.e("createWallet","生成助记词=${mnemonic}")
        if (mnemonic.isEmpty()){
            return@flow emit(null)
        }
        
        // 2. 从助记词生成凭证
        val credentials = createCredentialsFromMnemonic(mnemonic)
        
        // 3. 创建钱包文件
        val walletFileName = WalletUtils.generateWalletFile(
            request.password,
            credentials.ecKeyPair,
            request.walletDir,
            false
        )
        Log.e("createWallet","创建钱包文件=${walletFileName}")

        // 4. 保存钱包信息
        saveWalletInfo(request.walletName, credentials.address, walletFileName)
        
        val result = CreateWalletResult(
            mnemonic = mnemonic,
            walletAddress = credentials.address,
            walletFileName = walletFileName
        )
        emit(result)
    }.catch {
        LogUtils.e("创建钱包发生了异常,${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)

    override fun importWalletFromMnemonic(request: ImportWalletRequest): Flow<CreateWalletResult> = flow {
        val credentials = createCredentialsFromMnemonic(request.mnemonic)
        
        val walletFileName = WalletUtils.generateWalletFile(
            request.password,
            credentials.ecKeyPair,
            request.walletDir,
            false
        )
        
        // 保存钱包信息
        saveWalletInfo(request.walletName, credentials.address, walletFileName)
        
        val result = CreateWalletResult(
            mnemonic = request.mnemonic,
            walletAddress = credentials.address,
            walletFileName = walletFileName
        )
        emit(result)
    }

    override fun getBalance(address: String): Flow<BigInteger?> {
        return flow {
           val result =  web3.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .balance
            Log.e("查询到的余额",result.toString())
            emit(result)
        }.catch {
            LogUtils.e("查询余额发生了异常,${it.message}")
            emit(null)
        }.flowOn(Dispatchers.IO)
    }

    override fun generateMnemonic(): Flow<List<String>> = flow {
        val entropy = ByteArray(16) // 128 bits = 12 个单词
        SecureRandom().nextBytes(entropy)
        
        val mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy)
        emit(mnemonic)
    }

    private fun createCredentialsFromMnemonic(mnemonic: List<String>): Credentials {
        try {
            // 使用 bitcoinj 生成种子
            val seed = MnemonicCode.toSeed(mnemonic, "")
            
            // 生成主私钥
            val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)
            
            // 派生以太坊路径 m/44'/60'/0'/0/0
            val purposeKey = HDKeyDerivation.deriveChildKey(masterKey, ChildNumber(44, true))
            val coinKey = HDKeyDerivation.deriveChildKey(purposeKey, ChildNumber(60, true))
            val accountKey = HDKeyDerivation.deriveChildKey(coinKey, ChildNumber(0, true))
            val changeKey = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(0, false))
            val addressKey = HDKeyDerivation.deriveChildKey(changeKey, ChildNumber(0, false))
            
            val privateKeyHex = addressKey.privateKeyAsHex
            return Credentials.create(privateKeyHex)
        } catch (e: MnemonicException) {
            throw RuntimeException("助记词无效", e)
        }
    }

    private suspend fun saveWalletInfo(name: String, address: String, fileName: String) {
        walletStore.saveWalletInfo(address, name, fileName)
        // 设置为当前钱包
        walletStore.saveCurrentWalletAddress(address)
    }
}