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
        // 手动编码 getAmountsOut(uint256,address[]) 的 calldata
        // function selector: getAmountsOut(uint256,address[])
        val selector = "0xd06ca61f"
        val amountHex = Numeric.toHexStringNoPrefixZeroPadded(amountInWei, 64)
        // offset to address[] data (2 * 32 = 64 bytes = 0x40)
        val offsetHex = "0000000000000000000000000000000000000000000000000000000000000040"
        // array length
        val lengthHex = Numeric.toHexStringNoPrefixZeroPadded(BigInteger.valueOf(path.size.toLong()), 64)
        // address elements (left-padded to 32 bytes)
        val addressesHex = path.joinToString("") { addr ->
            val clean = addr.removePrefix("0x").removePrefix("0X").lowercase()
            clean.padStart(64, '0')
        }
        val callData = selector + amountHex + offsetHex + lengthHex + addressesHex

        val response = web3.ethCall(
            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                null, UNISWAP_V2_ROUTER, callData
            ),
            DefaultBlockParameterName.LATEST
        ).send()

        if (response.hasError()) {
            LogUtils.e("EthRepository", "getAmountsOut 失败: ${response.error.message}")
            emit(null)
            return@flow
        }

        // 解码返回值：uint256[] (dynamic array)
        // 格式: offset(32) + length(32) + elements(32 each)
        val hex = response.value.removePrefix("0x")
        if (hex.length < 128) {
            emit(null)
            return@flow
        }
        // skip offset (first 32 bytes), read length
        val arrayLength = BigInteger(hex.substring(64, 128), 16).toInt()
        if (arrayLength < 1) {
            emit(null)
            return@flow
        }
        // 最后一个元素就是 amountOut
        val lastElementStart = 128 + (arrayLength - 1) * 64
        val amountOut = BigInteger(hex.substring(lastElementStart, lastElementStart + 64), 16)
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
        val deadline = BigInteger.valueOf(System.currentTimeMillis() / 1000 + 1200)
        val isFromETH = fromToken.equals(com.data.wallet.util.WeiConverter.ETH_ADDRESS, ignoreCase = true)
        val isToETH = toToken.equals(com.data.wallet.util.WeiConverter.ETH_ADDRESS, ignoreCase = true)
        val weth = WETH_ADDRESS

        val callData: String
        val value: BigInteger

        if (isFromETH) {
            // swapExactETHForTokens(uint256,address[],address,uint256) selector: 0x7ff36ab5
            val path = listOf(weth, toToken)
            callData = encodeSwapCallData("7ff36ab5", amountOutMinWei, path, fromAddress, deadline)
            value = amountInWei
        } else if (isToETH) {
            // swapExactTokensForETH(uint256,uint256,address[],address,uint256) selector: 0x18cbafe5
            val path = listOf(fromToken, weth)
            callData = encodeSwapCallData("18cbafe5", amountInWei, amountOutMinWei, path, fromAddress, deadline)
            value = BigInteger.ZERO
        } else {
            // swapExactTokensForTokens(uint256,uint256,address[],address,uint256) selector: 0x38ed1739
            val path = listOf(fromToken, weth, toToken)
            callData = encodeSwapCallData("38ed1739", amountInWei, amountOutMinWei, path, fromAddress, deadline)
            value = BigInteger.ZERO
        }

        val tx = org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
            fromAddress, null, null, null, UNISWAP_V2_ROUTER, value, callData
        )
        val gasEstimate = web3.ethEstimateGas(tx).send()

        if (gasEstimate.hasError()) {
            LogUtils.e("EthRepository", "estimateGas 失败: ${gasEstimate.error.message}")
            emit(BigInteger.valueOf(200_000))
        } else {
            LogUtils.e("EthRepository", "estimateGas: ${gasEstimate.amountUsed}")
            emit(gasEstimate.amountUsed)
        }
    }.catch {
        LogUtils.e("EthRepository", "estimateGas 异常: ${it.message}")
        emit(BigInteger.valueOf(200_000))
    }.flowOn(Dispatchers.IO)

    // ==================== ABI 手动编码辅助方法 ====================

    /** 编码 swapExactETHForTokens: selector + amountOutMin + offset + to + deadline + path */
    private fun encodeSwapCallData(
        selector: String,
        amountOutMin: BigInteger,
        path: List<String>,
        to: String,
        deadline: BigInteger
    ): String {
        val sb = StringBuilder("0x$selector")
        sb.append(padUint256(amountOutMin))
        // offset to path array: 4 params * 32 = 128 = 0x80
        sb.append(padUint256(BigInteger.valueOf(128)))
        sb.append(padAddress(to))
        sb.append(padUint256(deadline))
        // path array
        sb.append(padUint256(BigInteger.valueOf(path.size.toLong())))
        path.forEach { sb.append(padAddress(it)) }
        return sb.toString()
    }

    /** 编码 swapExactTokensForTokens / swapExactTokensForETH: selector + amountIn + amountOutMin + offset + to + deadline + path */
    private fun encodeSwapCallData(
        selector: String,
        amountIn: BigInteger,
        amountOutMin: BigInteger,
        path: List<String>,
        to: String,
        deadline: BigInteger
    ): String {
        val sb = StringBuilder("0x$selector")
        sb.append(padUint256(amountIn))
        sb.append(padUint256(amountOutMin))
        // offset to path array: 5 params * 32 = 160 = 0xa0
        sb.append(padUint256(BigInteger.valueOf(160)))
        sb.append(padAddress(to))
        sb.append(padUint256(deadline))
        // path array
        sb.append(padUint256(BigInteger.valueOf(path.size.toLong())))
        path.forEach { sb.append(padAddress(it)) }
        return sb.toString()
    }

    private fun padUint256(value: BigInteger): String =
        Numeric.toHexStringNoPrefixZeroPadded(value, 64)

    private fun padAddress(address: String): String =
        address.removePrefix("0x").removePrefix("0X").lowercase().padStart(64, '0')
}
