package com.data.wallet.repo.impl

import android.util.Log
import com.data.wallet.api.UniswapTokenApi
import com.data.wallet.entity.MainWalletInfoEntity
import com.data.wallet.entity.TokenBalanceEntity
import com.data.wallet.entity.UniswapToken
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.model.TransactionModel
import com.data.wallet.repo.CreateWalletResult
import com.data.wallet.repo.IAlchemyRepository
import com.data.wallet.repo.IEthRepository
import com.data.wallet.repo.INetworkRepository
import com.data.wallet.repo.IWalletRepository
import com.data.wallet.storage.WalletStore
import com.google.gson.Gson
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Locale
import javax.inject.Inject

/**
 * 钱包仓库实现
 * 组合 EthRepository 和 AlchemyRepository，提供钱包相关的业务逻辑
 */
internal class WalletRepository @Inject constructor(
    private val walletStore: WalletStore,
    private val ethRepository: IEthRepository,
    private val alchemyRepository: IAlchemyRepository,
    private val netWorkRepository: INetworkRepository,
    private val uniswapTokenApi: UniswapTokenApi
) : IWalletRepository {

    override fun getWalletList(): Flow<List<String>> = walletStore.getWalletList()

    override fun sendTransaction(toAddress: String, amount: String, password: String): Flow<String?> = flow {
        // 同步网络配置
//        ethRepository.syncNetwork()

        // 获取当前钱包地址
        val currentAddress = walletStore.getWalletList().firstOrNull()?.firstOrNull()
        if (currentAddress.isNullOrBlank()) {
            LogUtils.e("WalletRepository", "当前钱包地址为空")
            emit(null)
            return@flow
        }

        // 获取钱包文件名
        val walletFileName = walletStore.getWalletFileName(currentAddress).firstOrNull()
        if (walletFileName.isNullOrBlank()) {
            LogUtils.e("WalletRepository", "钱包文件名为空")
            emit(null)
            return@flow
        }

        // 加载钱包凭证
        val walletFilePath = "${walletStore.getWalletDir()}/$walletFileName"
        val credentials = ethRepository.loadCredentials(password, walletFilePath)

        LogUtils.e("WalletRepository", "开始发送交易: from=${credentials.address}, to=$toAddress, amount=$amount ETH")

        // 发送交易
        val txHash = ethRepository.sendTransaction(credentials, toAddress, BigDecimal(amount))

        LogUtils.e("WalletRepository", "交易结果: txHash=$txHash")
        emit(txHash)
    }.catch {
        LogUtils.e("WalletRepository", "交易失败: ${it.message}")
        it.printStackTrace()
        emit(null)
    }.flowOn(Dispatchers.IO)

    override fun getMainWalletInfo(): Flow<MainWalletInfoEntity?> {
        return netWorkRepository.getCurrentNetwork().flatMapLatest { config ->
            flow {
//                ethRepository.syncNetwork()
                
                val walletList = getWalletList().firstOrNull()
                if (walletList?.isNotEmpty() == true) {
                    val currentAddress = walletList.firstOrNull() ?: ""
                    LogUtils.e("WalletRepository", "当前地址: $currentAddress")

                    // 获取代币余额
                    val tokenBalances = alchemyRepository.getTokenBalances(currentAddress).firstOrNull()
                    LogUtils.e("WalletRepository", "代币余额: ${Gson().toJson(tokenBalances)}")

                    // 获取 ETH 余额
                    val walletBalance = ethRepository.getBalance(currentAddress).firstOrNull() ?: BigInteger.ZERO
                    val balance = convertWeiToEth(walletBalance)

                    // 获取交易记录
                    val transactions = ethRepository.getTransactions(currentAddress, 5).firstOrNull() ?: emptyList()

                    // 获取 ETH 价格
                    val ethPrice = ethRepository.getEthPrice().firstOrNull() ?: 0.0
                    val ethUsdValue = balance.toDouble() * ethPrice
                    val ethValue = String.format(Locale.US, "$%.2f", ethUsdValue)

                    // 总资产 = ETH USD 价值 + 所有代币余额求和
                    val tokenTotalBalance = tokenBalances?.sumOf { 
                        it.balance.toDoubleOrNull() ?: 0.0 
                    } ?: 0.0
                    val totalValue = String.format(Locale.US, "$%.2f", ethUsdValue + tokenTotalBalance)
                    LogUtils.e("总资产", totalValue)

                    emit(
                        MainWalletInfoEntity(
                            currentAddress = currentAddress,
                            walletList = walletList,
                            balance = String.format(Locale.US, "%.4f", balance),
                            ethValue = ethValue,
                            totalValue = totalValue,
                            tokenBalances = tokenBalances ?: emptyList(),
                            transaction = transactions
                        )
                    )
                } else {
                    emit(null)
                }
            }.catch {
                it.printStackTrace()
                emit(null)
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?> = flow {
        // 生成助记词
        val mnemonic = generateMnemonic().firstOrNull() ?: emptyList()
        Log.e("WalletRepository", "生成助记词: $mnemonic")
        if (mnemonic.isEmpty()) {
            emit(null)
            return@flow
        }

        // 从助记词生成凭证
        val credentials = createCredentialsFromMnemonic(mnemonic)

        // 创建钱包文件
        val walletFileName = WalletUtils.generateWalletFile(
            request.password,
            credentials.ecKeyPair,
            request.walletDir,
            false
        )
        Log.e("WalletRepository", "创建钱包文件: $walletFileName")

        // 保存钱包信息
        saveWalletInfo(request.walletName, credentials.address, walletFileName)

        emit(CreateWalletResult(mnemonic, credentials.address, walletFileName))
    }.catch {
        LogUtils.e("WalletRepository", "创建钱包异常: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)

    override fun importWalletFromMnemonic(request: ImportWalletRequest): Flow<CreateWalletResult> = flow {
        if (!request.walletDir.exists()) request.walletDir.mkdirs()

        val credentials = createCredentialsFromMnemonic(request.mnemonic)

        val walletFileName = WalletUtils.generateWalletFile(
            request.password,
            credentials.ecKeyPair,
            request.walletDir,
            false
        )

        saveWalletInfo(request.walletName, credentials.address, walletFileName)

        emit(CreateWalletResult(request.mnemonic, credentials.address, walletFileName))
    }.flowOn(Dispatchers.IO)

    override fun getBalance(address: String): Flow<BigInteger?> = ethRepository.getBalance(address)

    override fun generateMnemonic(): Flow<List<String>> = flow {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        emit(MnemonicCode.INSTANCE.toMnemonic(entropy))
    }

    override fun getTransactions(address: String, limit: Int): Flow<List<TransactionModel>> =
        ethRepository.getTransactions(address, limit)

    override fun getEthPrice(): Flow<Double?> = ethRepository.getEthPrice()

    override fun getTokenBalances(address: String): Flow<List<TokenBalanceEntity>> =
        alchemyRepository.getTokenBalances(address)

    // ==================== 私有方法 ====================

    private fun createCredentialsFromMnemonic(mnemonic: List<String>): Credentials {
        try {
            val seed = MnemonicCode.toSeed(mnemonic, "")
            val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)

            // 派生以太坊路径 m/44'/60'/0'/0/0
            val purposeKey = HDKeyDerivation.deriveChildKey(masterKey, ChildNumber(44, true))
            val coinKey = HDKeyDerivation.deriveChildKey(purposeKey, ChildNumber(60, true))
            val accountKey = HDKeyDerivation.deriveChildKey(coinKey, ChildNumber(0, true))
            val changeKey = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(0, false))
            val addressKey = HDKeyDerivation.deriveChildKey(changeKey, ChildNumber(0, false))

            return Credentials.create(addressKey.privateKeyAsHex)
        } catch (e: MnemonicException) {
            throw RuntimeException("助记词无效", e)
        }
    }

    private suspend fun saveWalletInfo(name: String, address: String, fileName: String) {
        walletStore.saveWalletInfo(address, name, fileName)
        walletStore.saveCurrentWalletAddress(address)
    }

    private fun convertWeiToEth(wei: BigInteger?): BigDecimal {
        val weiInEth = BigDecimal("1000000000000000000")
        return BigDecimal(wei ?: BigInteger.ZERO).divide(weiInEth)
    }

    /** 热门币种符号，用于排序 */
    private val popularSymbols = listOf(
        "USDT", "USDC", "DAI", "WBTC", "WETH", "UNI", "LINK",
        "AAVE", "MKR", "SNX", "COMP", "CRV", "LDO", "RPL", "MATIC",
        "SHIB", "APE", "PEPE", "ARB"
    )

    override fun getUniswapTokenList(): Flow<List<UniswapToken>> = flow {
        val response = uniswapTokenApi.getTokenList("https://tokens.uniswap.org/")
        val mainnetTokens = response?.tokens
            ?.filter { it.chainId == 1 }
            ?.distinctBy { it.symbol }

        val sorted = mainnetTokens?.sortedBy { token ->
            val idx = popularSymbols.indexOf(token.symbol)
            if (idx >= 0) idx else Int.MAX_VALUE
        }?.take(20) ?: emptyList()

        emit(sorted)
    }.catch {
        LogUtils.e("WalletRepository", "获取 Uniswap 代币列表失败: ${it.message}")
        emit(emptyList())
    }.flowOn(Dispatchers.IO)
}
