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
        /** Uniswap V2 Router 合约地址 */
        private const val UNISWAP_V2_ROUTER = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
        /** WETH 合约地址（主网） */
        const val WETH_ADDRESS = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
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

    /**
     * 通过 Uniswap V2 Router 的 getAmountsOut 查询兑换报价
     */
    override fun getAmountsOut(amountInWei: BigInteger, path: List<String>): Flow<BigInteger?> = flow {
        val pathAddresses = path.map { org.web3j.abi.datatypes.Address(it) }
        val function = org.web3j.abi.FunctionEncoder.encode(
            org.web3j.abi.datatypes.Function(
                "getAmountsOut",
                listOf(
                    org.web3j.abi.datatypes.generated.Uint256(amountInWei),
                    org.web3j.abi.DynamicArray(org.web3j.abi.datatypes.Address::class.java, pathAddresses)
                ),
                listOf(object : org.web3j.abi.TypeReference<org.web3j.abi.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>>() {})
            )
        )

        val response = web3.ethCall(
            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                null, UNISWAP_V2_ROUTER, function
            ),
            org.web3j.protocol.core.DefaultBlockParameterName.LATEST
        ).send()

        if (response.hasError()) {
            LogUtils.e("EthRepository", "getAmountsOut 失败: ${response.error.message}")
            emit(null)
            return@flow
        }

        val decoded = org.web3j.abi.FunctionReturnDecoder.decode(
            response.value,
            org.web3j.abi.Utils.convert(
                listOf(object : org.web3j.abi.TypeReference<org.web3j.abi.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>>() {})
            )
        )

        @Suppress("UNCHECKED_CAST")
        val amounts = decoded[0] as org.web3j.abi.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>
        val amountOut = amounts.value.lastOrNull()?.value
        LogUtils.e("EthRepository", "getAmountsOut: $amountInWei -> $amountOut")
        emit(amountOut)
    }.catch {
        LogUtils.e("EthRepository", "getAmountsOut 异常: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)

    /**
     * 获取当前 Gas 价格 (Wei)
     */
    override fun getGasPrice(): Flow<BigInteger?> = flow {
        val gasPrice = web3.ethGasPrice().send().gasPrice
        LogUtils.e("EthRepository", "当前 gasPrice: $gasPrice")
        emit(gasPrice)
    }.catch {
        LogUtils.e("EthRepository", "获取 gasPrice 失败: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)

    /**
     * 预估 Swap 交易的 Gas 用量
     * 通过构造 Uniswap V2 Router 的 swapExactETHForTokens / swapExactTokensForTokens calldata 来预估
     */
    override fun estimateSwapGas(
        fromAddress: String,
        fromToken: String,
        toToken: String,
        amountInWei: BigInteger,
        amountOutMinWei: BigInteger
    ): Flow<BigInteger?> = flow {
        val deadline = BigInteger.valueOf(System.currentTimeMillis() / 1000 + 1200) // 20分钟
        val isFromETH = fromToken.equals(com.data.wallet.util.WeiConverter.ETH_ADDRESS, ignoreCase = true)
        val weth = WETH_ADDRESS

        val callData: String
        val value: BigInteger

        if (isFromETH) {
            // swapExactETHForTokens(uint256 amountOutMin, address[] path, address to, uint256 deadline)
            val path = listOf(org.web3j.abi.datatypes.Address(weth), org.web3j.abi.datatypes.Address(toToken))
            callData = org.web3j.abi.FunctionEncoder.encode(
                org.web3j.abi.datatypes.Function(
                    "swapExactETHForTokens",
                    listOf(
                        org.web3j.abi.datatypes.generated.Uint256(amountOutMinWei),
                        org.web3j.abi.DynamicArray(org.web3j.abi.datatypes.Address::class.java, path),
                        org.web3j.abi.datatypes.Address(fromAddress),
                        org.web3j.abi.datatypes.generated.Uint256(deadline)
                    ),
                    emptyList()
                )
            )
            value = amountInWei
        } else {
            // swapExactTokensForETH 或 swapExactTokensForTokens
            val isToETH = toToken.equals(com.data.wallet.util.WeiConverter.ETH_ADDRESS, ignoreCase = true)
            val path = if (isToETH) {
                listOf(org.web3j.abi.datatypes.Address(fromToken), org.web3j.abi.datatypes.Address(weth))
            } else {
                listOf(org.web3j.abi.datatypes.Address(fromToken), org.web3j.abi.datatypes.Address(weth), org.web3j.abi.datatypes.Address(toToken))
            }
            val funcName = if (isToETH) "swapExactTokensForETH" else "swapExactTokensForTokens"
            callData = org.web3j.abi.FunctionEncoder.encode(
                org.web3j.abi.datatypes.Function(
                    funcName,
                    listOf(
                        org.web3j.abi.datatypes.generated.Uint256(amountInWei),
                        org.web3j.abi.datatypes.generated.Uint256(amountOutMinWei),
                        org.web3j.abi.DynamicArray(org.web3j.abi.datatypes.Address::class.java, path),
                        org.web3j.abi.datatypes.Address(fromAddress),
                        org.web3j.abi.datatypes.generated.Uint256(deadline)
                    ),
                    emptyList()
                )
            )
            value = BigInteger.ZERO
        }

        val tx = org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
            fromAddress, null, null, null, UNISWAP_V2_ROUTER, value, callData
        )
        val gasEstimate = web3.ethEstimateGas(tx).send()

        if (gasEstimate.hasError()) {
            LogUtils.e("EthRepository", "estimateGas 失败: ${gasEstimate.error.message}")
            // Swap 交易 gas 预估失败时使用默认值 200000
            emit(BigInteger.valueOf(200_000))
        } else {
            LogUtils.e("EthRepository", "estimateGas: ${gasEstimate.amountUsed}")
            emit(gasEstimate.amountUsed)
        }
    }.catch {
        LogUtils.e("EthRepository", "estimateGas 异常: ${it.message}")
        emit(BigInteger.valueOf(200_000))
    }.flowOn(Dispatchers.IO)
}
