package com.data.wallet.repo.impl

import AESCryptoUtils
import android.util.Log
import com.data.wallet.api.NodeRealApi
import com.data.wallet.api.NodeRealRequest
import com.data.wallet.api.TransactionParams
import com.data.wallet.model.NetworkInfo
import com.data.wallet.model.TransactionModel
import com.data.wallet.repo.IEthRepository
import com.data.wallet.repo.INetworkRepository
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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
import javax.inject.Inject

/**
 * ETH 链上操作仓库实现
 * 负责 ETH 余额查询、交易发送、价格获取等链上操作
 */
internal class EthRepository @Inject constructor(
    private val apiService: NodeRealApi,
    private val netWorkRepository: INetworkRepository
) : IEthRepository {

    companion object {
        private const val NOTE_KEY = "54f700c58aeb4d2fb2620b817759894e"
        private const val CHAINLINK_ETH_USD_ADDRESS = "0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419"
    }

    private val nodeUrl = "https://eth-mainnet.nodereal.io/v1/$NOTE_KEY"
    private var netWorkUrl = nodeUrl
    private var web3: Web3j = Web3j.build(HttpService(nodeUrl))

    /**
     * 同步网络配置
     */
    suspend fun syncNetwork() {
        val current = netWorkRepository.getCurrentNetwork().firstOrNull()
        checkNetWork(current)
    }

    private fun checkNetWork(current: NetworkInfo?) {
        if (current == null) return
        try {
            val url = current.rpcUrl + AESCryptoUtils.decrypt(current.apiKey)
            if (url == netWorkUrl) return
            netWorkUrl = url
            web3 = Web3j.build(HttpService(netWorkUrl))
        } catch (e: Exception) {
            LogUtils.e("EthRepository", "解密 apiKey 失败: ${e.message}")
        }
    }

    override fun getBalance(address: String): Flow<BigInteger?> = flow {
        val result = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST)
            .send()
            .balance
        Log.e("EthRepository", "查询到的余额: $result")
        emit(result)
    }.catch {
        Log.e("EthRepository", "查询余额异常: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)

    override suspend fun sendTransaction(
        credentials: Credentials,
        toAddress: String,
        amount: BigDecimal
    ): String? = withContext(Dispatchers.IO) {
        val nonce = web3.ethGetTransactionCount(
            credentials.address, DefaultBlockParameterName.PENDING
        ).send().transactionCount

        val latestBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().block
        val baseFee = latestBlock.baseFeePerGas
        val maxPriorityFee = BigInteger.valueOf(2_000_000_000L) // 2 Gwei
        val maxFeePerGas = baseFee.multiply(BigInteger.TWO).add(maxPriorityFee)

        val gasLimit = BigInteger.valueOf(21000)
        val value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        val chainId = 1L

        val rawTransaction = RawTransaction.createTransaction(
            chainId, nonce, gasLimit, toAddress, value, "", maxPriorityFee, maxFeePerGas
        )

        val signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
        val hexValue = Numeric.toHexString(signedMessage)

        val response = web3.ethSendRawTransaction(hexValue).send()
        if (response.hasError()) {
            LogUtils.e("EthRepository", "节点返回错误: ${response.error.message}")
            null
        } else {
            response.transactionHash
        }
    }

    override fun getTransactions(address: String, limit: Int): Flow<List<TransactionModel>> = flow {
        val request = NodeRealRequest(
            params = listOf(
                TransactionParams(
                    address = address,
                    maxCount = "0x${limit.toString(16)}"
                )
            )
        )

        val response = apiService.getTransactions(url = nodeUrl, request = request)
        val transactions = response?.result?.transfers?.map { tx ->
            TransactionModel(
                hash = tx.hash,
                from = tx.from,
                to = tx.to,
                value = tx.value,
                gasUsed = tx.gasUsed,
                gasPrice = tx.gasPrice,
                blockNumber = tx.blockNum,
                isReceive = tx.to.equals(address, ignoreCase = true),
                timestamp = tx.blockTimeStamp.toLong() * 1000
            )
        } ?: emptyList()
        emit(transactions)
    }.catch {
        LogUtils.e("EthRepository", "获取交易记录失败: ${it.message}")
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getEthPrice(): Flow<Double?> = flow {
        val price = getEthPriceFromChainlink()
        LogUtils.e("EthRepository", "ETH/USD 价格 (Chainlink): $price")
        emit(price)
    }.catch {
        LogUtils.e("EthRepository", "获取ETH价格失败: ${it.message}")
        emit(0.0)
    }.flowOn(Dispatchers.IO)

    /**
     * 通过 Chainlink 预言机获取 ETH/USD 价格
     */
    private suspend fun getEthPriceFromChainlink(): Double = withContext(Dispatchers.IO) {
        val function = org.web3j.abi.FunctionEncoder.encode(
            org.web3j.abi.datatypes.Function(
                "latestRoundData",
                emptyList(),
                listOf(
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint80::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Int256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint80::class.java)
                )
            )
        )

        val response = web3.ethCall(
            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                null, CHAINLINK_ETH_USD_ADDRESS, function
            ),
            DefaultBlockParameterName.LATEST
        ).send()

        if (response.hasError()) {
            LogUtils.e("EthRepository", "Chainlink 调用失败: ${response.error.message}")
            return@withContext 0.0
        }

        val result = org.web3j.abi.FunctionReturnDecoder.decode(
            response.value,
            org.web3j.abi.Utils.convert(
                listOf(
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint80::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Int256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint256::class.java),
                    org.web3j.abi.TypeReference.create(org.web3j.abi.datatypes.generated.Uint80::class.java)
                )
            )
        )

        val answer = (result[1] as org.web3j.abi.datatypes.generated.Int256).value
        answer.toDouble() / 100_000_000.0
    }

    override fun isValidAddress(address: String): Boolean = WalletUtils.isValidAddress(address)

    override suspend fun loadCredentials(password: String, walletFilePath: String): Credentials =
        withContext(Dispatchers.IO) {
            WalletUtils.loadCredentials(password, walletFilePath)
        }
}
