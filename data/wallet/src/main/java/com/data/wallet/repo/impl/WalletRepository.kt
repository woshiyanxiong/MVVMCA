package com.data.wallet.repo.impl

import AESCryptoUtils
import android.util.Log
import com.data.wallet.api.AlchemyApi
import com.data.wallet.entity.AlchemyTokenBalanceRequest
import com.data.wallet.entity.AlchemyTokenMetadataRequest
import com.data.wallet.entity.AlchemyTokenPriceRequest
import com.data.wallet.entity.AlchemyTokenPriceParams
import com.data.wallet.entity.AlchemyTokenAddress
import com.data.wallet.api.CoinGeckoApi
import com.data.wallet.api.NodeRealApi
import com.data.wallet.api.NodeRealRequest
import com.data.wallet.api.TransactionParams
import com.data.wallet.entity.MainWalletInfoEntity
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.model.NetworkInfo
import com.data.wallet.model.TransactionModel
import com.data.wallet.entity.TokenBalanceEntity
import com.data.wallet.repo.CreateWalletResult
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
    private val alchemyApi: AlchemyApi,
    private val netWorkRepository: INetworkRepository
) : IWalletRepository {
    private val noteKey = "54f700c58aeb4d2fb2620b817759894e"
    private val newNoteKey = "dba7fde082124c78b2809090d48bd69a"
    private val ETH_API_KEY = "N5SG14C1TXZN7917ECQY6KUFW7BEF7M1J3"
    private val ALCHEMY_URL = "https://eth-mainnet.g.alchemy.com/v2/nUELTIlKKB-hJR3k6X3FN"

    private val nodeUrl = "https://eth-mainnet.nodereal.io/v1/${noteKey}"
    private val infuraURL = "https://mainnet.infura.io/v3/1659dfb40aa24bbb8153a677b98064d7"

    private val test =
        "\"fatal, stand, various, brisk, aisle, object, proof, skull, else, runway, jaguar, unique\""

    /**
     * [police, country, actual, grief, electric, flat, scan, shadow, tip, skate, boil, begin]
     *
     */
    private val testHome = ""

    private var netWorkUrl = nodeUrl

    private var web3: Web3j = Web3j.build(HttpService(nodeUrl))

    /**
     * 检查当前网络是否是创建的网络请求
     */
    private fun checkNetWork(current: NetworkInfo?) {
        if (current == null) {
            return
        }
        try {
            val url = current.rpcUrl + AESCryptoUtils.decrypt(current.apiKey)
            if (url == netWorkUrl) {
                return
            }
            netWorkUrl = url
            web3 = Web3j.build(HttpService(netWorkUrl))
        } catch (e: Exception) {
            LogUtils.e("解密 apiKey 失败: ${e.message}")
            // 使用默认 nodeUrl
        }
    }

    override fun getWalletList(): Flow<List<String>> {
        return walletStore.getWalletList()
    }

    override fun sendTransaction(toAddress: String, amount: String, password: String): Flow<String?> = flow {
        // 0. 同步当前网络配置
        val currentNetwork = netWorkRepository.getCurrentNetwork().firstOrNull()
        checkNetWork(currentNetwork)

        // 1. 获取当前钱包地址
        val currentAddress = walletStore.getWalletList().firstOrNull()?.firstOrNull()
        if (currentAddress.isNullOrBlank()) {
            LogUtils.e("sendTransaction", "当前钱包地址为空")
            emit(null)
            return@flow
        }

        // 2. 获取钱包文件名
        val walletFileName = walletStore.getWalletFileName(currentAddress).firstOrNull()
        if (walletFileName.isNullOrBlank()) {
            LogUtils.e("sendTransaction", "钱包文件名为空")
            emit(null)
            return@flow
        }

        // 3. 加载钱包凭证
        val walletFilePath = "${walletStore.getWalletDir()}/$walletFileName"
        val credentials = loadWalletFromFile(password, walletFilePath)
        
        LogUtils.e("sendTransaction", "开始发送交易: from=${credentials.address}, to=$toAddress, amount=$amount ETH, rpc=$netWorkUrl")

        // 4. 发送交易
        val txHash = sendTransaction(credentials, toAddress, BigDecimal(amount))
        
        LogUtils.e("sendTransaction", "交易成功: txHash=$txHash")
        emit(txHash)
    }.catch {
        LogUtils.e("sendTransaction", "交易失败: ${it.message}")
        it.printStackTrace()
        emit(null)
    }.flowOn(Dispatchers.IO)


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

    // 发送交易 (EIP-1559)
    suspend fun sendTransaction(
        credentials: Credentials,
        toAddress: String,
        amount: BigDecimal
    ): String? = withContext(Dispatchers.IO) {
        val nonce = web3.ethGetTransactionCount(
            credentials.address, DefaultBlockParameterName.PENDING
        ).send().transactionCount

        // 获取最新区块的 baseFee
        val latestBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().block
        val baseFee = latestBlock.baseFeePerGas

        // maxPriorityFeePerGas: 给矿工的小费，2 Gwei 是常用值
        val maxPriorityFee = BigInteger.valueOf(2_000_000_000L) // 2 Gwei

        // maxFeePerGas: baseFee * 2 + 小费，留足空间应对 baseFee 波动
        val maxFeePerGas = baseFee.multiply(BigInteger.TWO).add(maxPriorityFee)

        val gasLimit = BigInteger.valueOf(21000)
        val value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        val chainId = 1L // ETH 主网

        val rawTransaction = RawTransaction.createTransaction(
            chainId,
            nonce,
            gasLimit,
            toAddress,
            value,
            "",
            maxPriorityFee,
            maxFeePerGas
        )

        val signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
        val hexValue = Numeric.toHexString(signedMessage)

        val response = web3.ethSendRawTransaction(hexValue).send()
        if (response.hasError()) {
            LogUtils.e("sendTransaction", "节点返回错误: ${response.error.message}")
            null
        } else {
            response.transactionHash
        }
    }

    // 验证地址格式
    fun isValidAddress(address: String): Boolean {
        return WalletUtils.isValidAddress(address)
    }

    override fun getMainWalletInfo(): Flow<MainWalletInfoEntity?> {
        return netWorkRepository.getCurrentNetwork().flatMapLatest { config ->
            flow {
                checkNetWork(config)
                val walletList = getWalletList().firstOrNull()
                if (walletList?.isNotEmpty() == true) {
                    val currentAddress = walletList.firstOrNull() ?: ""
                    LogUtils.e("当前地址: $currentAddress")
                    val test = getTokenBalances(currentAddress).firstOrNull()
                    LogUtils.e("聚合","${Gson().toJson(test)}")
                    // 获取ETH余额
                    val walletBalance = getBalance(currentAddress).firstOrNull() ?: BigInteger("0")
                    val balance = convertWeiToEth(walletBalance)
                    val transactions =
                        getTransactions(currentAddress, 5).firstOrNull() ?: emptyList()
                    val ethPrice = 2000.0
                    val ethValue =
                        String.format(Locale.US, "$%.2f", (balance.toDouble() * ethPrice))
                    LogUtils.e("当前地址", currentAddress)
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
    }

    override fun createWallet(request: CreateWalletRequest): Flow<CreateWalletResult?> = flow {
//        val currentAddress = walletStore.getCurrentWalletAddress().firstOrNull()
//        if (!currentAddress.isNullOrBlank()) {
//            val file = walletStore.getWalletFileName(currentAddress).firstOrNull() ?: ""
//            val entity = CreateWalletResult(
//                mnemonic = ("fatal, stand, various, brisk, aisle, object, proof, skull, else, runway, jaguar, unique").split(
//                    ", "
//                ),
//                walletAddress = currentAddress,
//                walletFileName = file
//            )
//            return@flow emit(entity)
//        }
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
            if (!request.walletDir.exists()) request.walletDir.mkdirs()

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
        }.flowOn(Dispatchers.IO)

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

    override fun getTokenBalances(address: String): Flow<List<TokenBalanceEntity>> = flow {
        val request = AlchemyTokenBalanceRequest(
            params = listOf(address, "erc20")
        )
        val response = alchemyApi.getTokenBalances(ALCHEMY_URL, request)
        val tokenBalances = response?.result?.tokenBalances
        if (tokenBalances.isNullOrEmpty()) {
            emit(emptyList())
            return@flow
        }

        // 过滤掉余额为0的代币
        val nonZeroTokens = tokenBalances.filter { token ->
            val balance = token.tokenBalance ?: "0x0"
            balance != "0x0" && balance != "0x" && balance != "0"
        }

        // 查询每个代币的元数据
        val result = nonZeroTokens.mapNotNull { token ->
            try {
                val metaRequest = AlchemyTokenMetadataRequest(
                    params = listOf(token.contractAddress)
                )
                val metaResponse = alchemyApi.getTokenMetadata(ALCHEMY_URL, metaRequest)
                val meta = metaResponse?.result ?: return@mapNotNull null
                val decimals = meta.decimals ?: 18
                val rawBalance = BigInteger(token.tokenBalance!!.removePrefix("0x"), 16)
                val divisor = BigDecimal.TEN.pow(decimals)
                val balance = BigDecimal(rawBalance).divide(divisor, decimals, java.math.RoundingMode.DOWN)

                TokenBalanceEntity(
                    contractAddress = token.contractAddress,
                    name = meta.name ?: "Unknown",
                    symbol = meta.symbol ?: "???",
                    decimals = decimals,
                    balance = balance.stripTrailingZeros().toPlainString(),
                    logo = meta.logo
                )
            } catch (e: Exception) {
                LogUtils.e("getTokenBalances", "获取代币元数据失败: ${e.message}")
                null
            }
        }
        emit(result)
    }.catch {
        LogUtils.e("getTokenBalances", "获取代币余额失败: ${it.message}")
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    /**
     * 获取代币的 USD 价格
     * 
     * @param contractAddresses 代币合约地址列表，ETH 使用特殊地址 "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
     * @return Map<合约地址, USD价格>
     */
    override fun getTokenPrices(contractAddresses: List<String>): Flow<Map<String, Double>> = flow {
        if (contractAddresses.isEmpty()) {
            emit(emptyMap())
            return@flow
        }

        val addresses = contractAddresses.map { address ->
            AlchemyTokenAddress(
                network = "eth-mainnet",
                address = address
            )
        }

        val request = AlchemyTokenPriceRequest(
            params = listOf(AlchemyTokenPriceParams(addresses = addresses))
        )

        val response = alchemyApi.getTokenPrices(ALCHEMY_URL, request)
        val priceMap = mutableMapOf<String, Double>()

        response?.result?.data?.forEach { tokenData ->
            val address = tokenData.address ?: return@forEach
            val usdPrice = tokenData.prices?.find { it.currency == "usd" }?.value?.toDoubleOrNull() ?: 0.0
            priceMap[address.lowercase()] = usdPrice
        }

        emit(priceMap)
    }.catch {
        LogUtils.e("getTokenPrices", "获取代币价格失败: ${it.message}")
        emit(emptyMap())
    }.flowOn(Dispatchers.IO)

    /**
     * 获取 ETH 的 USD 价格
     * ETH 原生代币使用特殊地址: 0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE
     */
    override fun getEthPriceFromAlchemy(): Flow<Double> = flow {
        val ethAddress = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        val prices = getTokenPrices(listOf(ethAddress)).firstOrNull() ?: emptyMap()
        emit(prices[ethAddress.lowercase()] ?: 0.0)
    }.catch {
        LogUtils.e("getEthPriceFromAlchemy", "获取 ETH 价格失败: ${it.message}")
        emit(0.0)
    }.flowOn(Dispatchers.IO)
}