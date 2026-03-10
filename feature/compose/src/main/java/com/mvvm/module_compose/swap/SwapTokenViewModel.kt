package com.mvvm.module_compose.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import com.data.wallet.util.WeiConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * 兑换代币信息
 */
data class SwapTokenInfo(
    val symbol: String,
    val name: String,
    val address: String,
    val decimals: Int = 18,
    val logoUrl: String? = null,
    val balance: String = ""
)

/**
 * 兑换页面状态
 */
data class SwapTokenState(
    val fromToken: SwapTokenInfo = SwapTokenInfo("ETH", "Ethereum", WeiConverter.ETH_ADDRESS, 18, null),
    val toToken: SwapTokenInfo = SwapTokenInfo("USDT", "Tether USD", "0xdAC17F958D2ee523a2206206994597C13D831ec7", 6, null),
    val fromAmount: String = "",
    val toAmount: String = "",
    val fromBalance: String = "0.0",
    val exchangeRate: String = "0.0",
    val priceImpact: String = "< 0.01%",
    val minimumReceived: String = "0.0",
    val networkFee: String = "--",
    val fromAmountError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isQuoting: Boolean = false,
    val isValid: Boolean = false,
    val tokenList: List<SwapTokenInfo> = emptyList(),
    val isLoadingTokens: Boolean = true,
    val showFromTokenPicker: Boolean = false,
    val showToTokenPicker: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val passwordError: String? = null,
    val swapTxHash: String? = null,
    val swapSuccess: Boolean? = null
)

@HiltViewModel
class SwapTokenViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SwapTokenState())
    val state: StateFlow<SwapTokenState> = _state

    private var ethPrice: Double = 0.0
    private var ethBalance: String = "0.0"
    /** 缓存代币余额 map: contract address -> balance */
    private var tokenBalanceMap: Map<String, String> = emptyMap()
    /** 报价请求防抖 Job */
    private var quoteJob: Job? = null
    /** 缓存报价中的 gasPrice / gasLimit，执行 swap 时使用 */
    private var cachedGasPrice: java.math.BigInteger = java.math.BigInteger.ZERO
    private var cachedGasLimit: java.math.BigInteger = java.math.BigInteger.ZERO
    /** 缓存最小接收金额 Wei（0.5% 滑点） */
    private var cachedAmountOutMinWei: java.math.BigInteger = java.math.BigInteger.ZERO

    init {
        loadSwapData()
    }

    /**
     * 从 Repository 一次性获取兑换页面所需数据（代币列表 + 余额 + ETH价格）
     */
    private fun loadSwapData() {
        viewModelScope.launch {
            walletRepository.getSwapTokenData().collect { data ->
                ethPrice = data.ethPrice
                tokenBalanceMap = data.balanceMap
                ethBalance = data.balanceMap[WeiConverter.ETH_ADDRESS] ?: "0.0"

                // 构建代币列表（ETH + Uniswap 代币），附带余额
                val ethToken = SwapTokenInfo("ETH", "Ethereum", WeiConverter.ETH_ADDRESS, 18, null,
                    data.balanceMap[WeiConverter.ETH_ADDRESS] ?: "")
                val tokenInfoList = listOf(ethToken) + data.tokenList.map { token ->
                    SwapTokenInfo(
                        symbol = token.symbol,
                        name = token.name,
                        address = token.address,
                        decimals = token.decimals,
                        logoUrl = token.logoURI,
                        balance = data.balanceMap[token.address.lowercase()] ?: ""
                    )
                }

                val currentFromBalance = data.balanceMap[_state.value.fromToken.address.lowercase()] ?: "0.0"
                // 更新 fromToken，使其 balance 字段也同步
                val updatedFromToken = tokenInfoList.find { it.symbol == _state.value.fromToken.symbol }
                    ?: _state.value.fromToken.copy(balance = currentFromBalance)
                _state.value = _state.value.copy(
                    fromToken = updatedFromToken,
                    fromBalance = currentFromBalance,
                    tokenList = tokenInfoList,
                    isLoadingTokens = false,
                    toToken = tokenInfoList.find { it.symbol == _state.value.toToken.symbol } ?: _state.value.toToken
                )
            }
        }
    }

    /** 更新支付金额并触发报价请求 */
    fun updateFromAmount(amount: String) {
        _state.value = _state.value.copy(fromAmount = amount)
        requestQuote()
    }

    /** 交换支付/接收代币 */
    fun swapTokens() {
        val s = _state.value
        _state.value = s.copy(
            fromToken = s.toToken,
            toToken = s.fromToken,
            fromAmount = "",
            toAmount = "",
            fromBalance = s.toToken.balance.ifEmpty { "0.0" },
            fromAmountError = null,
            networkFee = "--",
            exchangeRate = "0.0",
            minimumReceived = "0.0",
            isValid = false
        )
    }

    fun showFromTokenPicker() {
        _state.value = _state.value.copy(showFromTokenPicker = true)
    }

    fun showToTokenPicker() {
        _state.value = _state.value.copy(showToTokenPicker = true)
    }

    fun dismissTokenPicker() {
        _state.value = _state.value.copy(showFromTokenPicker = false, showToTokenPicker = false)
    }

    fun selectFromToken(token: SwapTokenInfo) {
        _state.value = _state.value.copy(
            fromToken = token,
            showFromTokenPicker = false,
            fromAmount = "",
            toAmount = "",
            fromBalance = token.balance.ifEmpty { "0.0" }
        )
    }

    fun selectToToken(token: SwapTokenInfo) {
        _state.value = _state.value.copy(
            toToken = token,
            showToTokenPicker = false
        )
        requestQuote()
    }

    /**
     * 防抖请求链上报价（用户输入金额后 500ms 触发）
     */
    private fun requestQuote() {
        quoteJob?.cancel()
        val s = _state.value
        val fromAmount = s.fromAmount.toDoubleOrNull()

        if (fromAmount == null || fromAmount <= 0.0) {
            _state.value = s.copy(
                toAmount = "",
                exchangeRate = "0.0",
                minimumReceived = "0.0",
                networkFee = "--",
                isValid = false,
                isQuoting = false,
                fromAmountError = null,
                error = null
            )
            return
        }

        // 余额校验
        val balanceError = try {
            val bal = BigDecimal(s.fromBalance)
            if (BigDecimal(fromAmount) > bal) "余额不足" else null
        } catch (_: Exception) { null }

        _state.value = s.copy(isQuoting = true, fromAmountError = balanceError, error = null)

        quoteJob = viewModelScope.launch {
            delay(500) // 防抖
            walletRepository.getSwapQuote(
                fromToken = s.fromToken.address,
                toToken = s.toToken.address,
                amountIn = s.fromAmount,
                fromDecimals = s.fromToken.decimals,
                toDecimals = s.toToken.decimals
            ).collect { quote ->
                if (quote.error != null) {
                    _state.value = _state.value.copy(
                        toAmount = "",
                        exchangeRate = "0.0",
                        minimumReceived = "0.0",
                        networkFee = "--",
                        isQuoting = false,
                        isValid = false,
                        error = quote.error
                    )
                } else {
                    // 缓存 gas 参数和最小接收金额
                    cachedGasPrice = quote.gasPrice
                    cachedGasLimit = quote.gasLimit
                    // 计算 amountOutMinWei（0.5% 滑点）
                    val amountOutWei = BigDecimal(quote.amountOut)
                        .multiply(BigDecimal.TEN.pow(_state.value.toToken.decimals))
                        .toBigInteger()
                    cachedAmountOutMinWei = BigDecimal(amountOutWei)
                        .multiply(BigDecimal("0.995"))
                        .toBigInteger()

                    // Gas 费用校验：ETH 余额是否足够支付 Gas
                    val ethBal = BigDecimal(ethBalance)
                    val gasFee = BigDecimal(quote.gasFeeEth)
                    val isFromETH = _state.value.fromToken.address.equals(WeiConverter.ETH_ADDRESS, ignoreCase = true)

                    val gasError = if (isFromETH) {
                        // 支付 ETH 时：ETH余额 >= 输入金额 + Gas费
                        val totalNeeded = BigDecimal(_state.value.fromAmount).add(gasFee)
                        if (ethBal < totalNeeded) "ETH余额不足以支付金额+Gas费" else null
                    } else {
                        // 支付 ERC20 时：ETH余额 >= Gas费
                        if (ethBal < gasFee) "ETH余额不足以支付Gas费" else null
                    }

                    val finalError = balanceError ?: gasError
                    _state.value = _state.value.copy(
                        toAmount = quote.amountOut,
                        exchangeRate = quote.exchangeRate,
                        minimumReceived = quote.minimumReceived,
                        networkFee = "${quote.gasFeeEth} ETH",
                        isQuoting = false,
                        isValid = finalError == null && fromAmount > 0,
                        fromAmountError = balanceError,
                        error = gasError
                    )
                }
            }
        }
    }

    /** 点击兑换按钮，弹出密码确认框 */
    fun executeSwap() {
        if (!_state.value.isValid) return
        _state.value = _state.value.copy(showPasswordDialog = true, passwordError = null)
    }

    /** 关闭密码弹框 */
    fun dismissPasswordDialog() {
        _state.value = _state.value.copy(showPasswordDialog = false, passwordError = null)
    }

    /**
     * 确认密码后执行兑换交易
     * 调用 Repository 完成：加载凭证 → 检查授权 → approve → swap
     */
    fun confirmSwap(password: String) {
        val s = _state.value
        _state.value = s.copy(isLoading = true, passwordError = null)
        viewModelScope.launch {
            walletRepository.executeSwap(
                password = password,
                fromToken = s.fromToken.address,
                toToken = s.toToken.address,
                amountIn = s.fromAmount,
                fromDecimals = s.fromToken.decimals,
                amountOutMinWei = cachedAmountOutMinWei,
                gasPrice = cachedGasPrice,
                gasLimit = cachedGasLimit
            ).collect { txHash ->
                if (txHash != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showPasswordDialog = false,
                        swapTxHash = txHash,
                        swapSuccess = true,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        passwordError = "交易失败，请检查密码或网络"
                    )
                }
            }
        }
    }

    /** 重置兑换结果状态 */
    fun resetSwapResult() {
        _state.value = _state.value.copy(swapTxHash = null, swapSuccess = null)
    }
}
