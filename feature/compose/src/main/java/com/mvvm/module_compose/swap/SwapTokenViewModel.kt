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
    val showToTokenPicker: Boolean = false
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

    fun updateFromAmount(amount: String) {
        _state.value = _state.value.copy(fromAmount = amount)
        requestQuote()
    }

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
                    _state.value = _state.value.copy(
                        toAmount = quote.amountOut,
                        exchangeRate = quote.exchangeRate,
                        minimumReceived = quote.minimumReceived,
                        networkFee = "${quote.gasFeeEth} ETH",
                        isQuoting = false,
                        isValid = balanceError == null && fromAmount > 0,
                        error = null
                    )
                }
            }
        }
    }

    fun executeSwap() {
        if (!_state.value.isValid) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "兑换功能开发中，敬请期待"
            )
        }
    }
}
