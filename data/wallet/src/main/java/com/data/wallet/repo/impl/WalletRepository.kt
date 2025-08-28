package com.data.wallet.repo.impl

import android.util.Log
import com.data.wallet.api.CoinGeckoApi
import com.data.wallet.api.NodeRealApi
import com.data.wallet.api.NodeRealRequest
import com.data.wallet.api.TransactionParams
import com.data.wallet.entity.MainWalletInfoEntity
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.model.TransactionModel
import com.data.wallet.repo.CreateWalletResult
import com.data.wallet.repo.IWalletRepository
import com.data.wallet.storage.WalletStore
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Locale
import javax.inject.Inject

internal class WalletRepository @Inject constructor(
    private val walletStore: WalletStore,
    private val apiService: NodeRealApi,
    private val coinGeckoApi: CoinGeckoApi,
) : IWalletRepository {
    private val noteKey = "54f700c58aeb4d2fb2620b817759894e"
    private val ETH_API_KEY = "N5SG14C1TXZN7917ECQY6KUFW7BEF7M1J3"

    private val nodeUrl = "https://eth-mainnet.nodereal.io/v1/${noteKey}"
    private val infuraURL = "https://mainnet.infura.io/v3/1659dfb40aa24bbb8153a677b98064d7"

    // 创建Web3j实例
    private val web3: Web3j = Web3j.build(HttpService(nodeUrl))

    override fun getWalletList(): Flow<List<String>> {
        return walletStore.getWalletList()
    }


    // 创建新钱包
    suspend fun createWallet(password: String, walletDir: String): String =
        withContext(Dispatchers.IO) {
            val walletFile = WalletUtils.generateNewWalletFile(password, java.io.File(walletDir))
            walletFile
        }

    // 从私钥加载钱包
    fun loadWalletFromPrivateKey(privateKey: String): Credentials {
        return Credentials.create(privateKey)
    }

    // 从钱包文件加载
    suspend fun loadWalletFromFile(password: String, walletFilePath: String): Credentials =
        withContext(Dispatchers.IO) {
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

    override fun getMainWalletInfo(): Flow<MainWalletInfoEntity?> {
        return flow {
            val walletList = getWalletList().firstOrNull()
            if (walletList?.isNotEmpty() == true) {
                val currentAddress = walletList.firstOrNull() ?: ""
                LogUtils.e("当前地址: $currentAddress")
                // 获取ETH余额
                val walletBalance = getBalance(currentAddress).firstOrNull() ?: BigInteger("0")
                val balance = convertWeiToEth(walletBalance)
                val transactions = getTransactions(currentAddress, 5).firstOrNull() ?: emptyList()
                val ethPrice = 2000.0
                val ethValue = String.format(Locale.US,"$%.2f", (balance.toDouble() * ethPrice))
                return@flow emit(
                    MainWalletInfoEntity(
                        currentAddress = currentAddress,
                        walletList = walletList,
                        balance = String.format(Locale.US, "%.4f", balance),
                        ethValue = ethValue,
                        transaction = transactions
                    )
                )
            }
            emit(null)
        }.catch {
            it.printStackTrace()
            emit(null)
        }.flowOn(Dispatchers.IO)
    }

    override fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?> = flow {
        val currentAddress = walletStore.getCurrentWalletAddress().firstOrNull()
        if (!currentAddress.isNullOrBlank()) {
            val file = walletStore.getWalletFileName(currentAddress).firstOrNull() ?: ""
            val entity = CreateWalletResult(
                mnemonic = ("fatal, stand, various, brisk, aisle, object, proof, skull, else, runway, jaguar, unique").split(
                    ", "
                ),
                walletAddress = currentAddress,
                walletFileName = file
            )
            return@flow emit(entity)
        }
        // 1. 生成助记词
        val mnemonic = generateMnemonic().firstOrNull() ?: emptyList()
        Log.e("createWallet", "生成助记词=${mnemonic}")
        if (mnemonic.isEmpty()) {
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
        Log.e("createWallet", "创建钱包文件=${walletFileName}")

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

    override fun importWalletFromMnemonic(request: ImportWalletRequest): Flow<CreateWalletResult> =
        flow {
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
            val result = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .balance
            Log.e("查询到的余额", result.toString())
            emit(result)
        }.catch {
            Log.e("查询余额发生了异常", "${it.message}")
            emit(null)
        }.flowOn(Dispatchers.IO)
    }


    // 获取交易记录 - 使用 NodeReal API
    override fun getTransactions(address: String, limit: Int): Flow<List<TransactionModel>> = flow {
        val request = NodeRealRequest(
            params = listOf(
                TransactionParams(
                    address = address,
                    maxCount = "0x${limit.toString(16)}",
                )
            )
        )
        val currentAddress = getWalletList().firstOrNull()?.firstOrNull()

        val response = apiService.getTransactions(
            url = nodeUrl,
            request = request
        )
        LogUtils.e("NodeReal API 返回的数据: ${response}")
        val transactions = response?.result?.transfers?.map { tx ->
            TransactionModel(
                hash = tx.hash,
                from = tx.from,
                to = tx.to,
                value = tx.value,
                gasUsed = tx.gasUsed,
                gasPrice = tx.gasPrice,
                blockNumber = tx.blockNum,
                isReceive = tx.to == currentAddress,
                timestamp = tx.blockTimeStamp.toLong() * 1000
            )
        } ?: emptyList()
        emit(transactions)
    }.catch {
        it.printStackTrace()
        LogUtils.e("获取交易记录失败: ${it.message}")
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    // 获取ETH价格
    override fun getEthPrice(): Flow<Double?> = flow {
        val response = coinGeckoApi.getPrice()
        emit(response.ethereum.usd)
    }.catch {
        LogUtils.e("获取ETH价格失败: ${it.message}")
        emit(0.0)
    }.flowOn(Dispatchers.IO)

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

    private fun convertWeiToEth(wei: BigInteger?): BigDecimal {
        val weiInEth = BigDecimal("1000000000000000000") // 10^18
        return BigDecimal(wei).divide(weiInEth)
    }
    private fun formatAddress(address: String?): String {
        if (address.isNullOrEmpty()) return ""
        return if (address.length > 10) {
            "${address.substring(0, 6)}...${address.substring(address.length - 4)}"
        } else {
            address
        }
    }
}