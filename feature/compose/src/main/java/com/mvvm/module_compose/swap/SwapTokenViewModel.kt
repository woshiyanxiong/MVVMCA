package com.mvvm.module_compose.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.api.UniswapTokenApi
import com.data.wallet.entity.UniswapToken
import com.data.wallet.repo.IEthRepository
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 兑换代币信息
 */
data class SwapTokenInfo(
    val symbol: String,
    val name: String,
    val address: String,
    val decimals: Int = 18,
    val logoUrl: String? = null
)

/**
 * 兑换页面状态
 */
data class SwapTokenState(
    val fromToken: SwapTokenInfo = SwapTokenInfo("ETH", "Ethereum", "0x0000000000000000000000000000000000000000", 18, null),
    val toToken: SwapTokenInfo = SwapTokenInfo("USDT", "Tether USD", "0xdAC17F958D2ee523a2206206994597C13D831ec7", 6, null),
    val fromAmount: String = "",
    val toAmount: String = "",
    val fromBalance: String = "0.0",
    val exchangeRate: String = "0.0",
    val priceImpact: String = "< 0.01%",
    val minimumReceived: String = "0.0",
    val networkFee: String = "0.002",
    val fromAmountError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    // 币种列表
    val tokenList: List<SwapTokenInfo> = emptyList(),
    val isLoadingTokens: Boolean = true,
    // 弹框控制
    val showFromTokenPicker: Boolean = false,
    val showToTokenPicker: Boolean = false
)

@HiltViewModel
class SwapTokenViewModel @Inject constructor(
    private val walletRepository: IWalletRepository,
    private val ethRepository: IEthRepository,
    private val uniswapTokenApi: UniswapTokenApi
) : ViewModel() {

    private val _state = MutableStateFlow(SwapTokenState())
    val state: StateFlow<SwapTokenState> = _state

    private var ethPrice: Double = 0.0

    /** 热门币种符号，用于排序 */
    private val popularSymbols = listOf(
        "ETH", "USDT", "USDC", "DAI", "WBTC", "WETH", "UNI", "LINK",
        "AAVE", "MKR", "SNX", "COMP", "CRV", "LDO", "RPL", "MATIC",
        "SHIB", "APE", "PEPE", "ARB"
    )

    init {
        loadTokenList()
        loadEthBalance()
        loadEthPrice()
    }

    /**
     * 从 Uniswap Token List 加载主网热门币种前20
     */
    private fun loadTokenList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = uniswapTokenApi.getTokenList()
                val mainnetTokens = response?.tokens
                    ?.filter { it.chainId == 1 } // 只取主网
                    ?.distinctBy { it.symbol }

                // 按热门排序，取前20
                val sorted = mainnetTokens?.sortedBy { token ->
                    val idx = popularSymbols.indexOf(token.symbol)
                    if (idx >= 0) idx else Int.MAX_VALUE
                }?.take(20) ?: emptyList()

                // 加上 ETH（原生代币不在列表中）
                val ethToken = SwapTokenInfo("ETH", "Ethereum", "0x0000000000000000000000000000000000000000", 18, null)
                val tokenInfoList = listOf(ethToken) + sorted.map { token ->
                    SwapTokenInfo(
                        symbol = token.symbol,
                        name = token.name,
                        address = token.address,
                        decimals = token.decimals,
                        logoUrl = token.logoURI
                    )
                }

                _state.value = _state.value.copy(
                    tokenList = tokenInfoList,
                    isLoadingTokens = false,
                    // 更新 toToken 的 logoUrl
                    toToken = tokenInfoList.find { it.symbol == _state.value.toToken.symbol } ?: _state.value.toToken
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingTokens = false)
            }
        }
    }

    private fun loadEthBalance() {
        viewModelScope.launch {
            val address = walletRepository.getWalletList().firstOrNull()?.firstOrNull() ?: return@launch
            ethRepository.getBalance(address).collect { balance ->
                if (balance != null) {
                    val ethBalance = BigDecimal(balance).divide(BigDecimal("1000000000000000000"), 4, RoundingMode.DOWN).toPlainString()
                    _state.value = _state.value.copy(fromBalance = ethBalance)
                }
            }
        }
    }

    private fun loadEthPrice() {
        viewModelScope.launch {
            ethRepository.getEthPrice().collect { price ->
                ethPrice = price ?: 0.0
                recalculate()
            }
        }
    }

    fun updateFromAmount(amount: String) {
        _state.value = _state.value.copy(fromAmount = amount)
        recalculate()
    }

    /**
     * 交换支付/接收代币
     */
    fun swapTokens() {
        val s = _state.value
        _state.value = s.copy(
            fromToken = s.toToken,
            toToken = s.fromToken,
            fromAmount = "",
            toAmount = "",
            fromBalance = "0.0",
            fromAmountError = null
        )
        // 如果新的 fromToken 是 ETH，重新加载余额
        if (_state.value.fromToken.symbol == "ETH") {
            loadEthBalance()
        }
        recalculate()
    }

    /** 显示支付代币选择弹框 */
    fun showFromTokenPicker() {
        _state.value = _state.value.copy(showFromTokenPicker = true)
    }

    /** 显示接收代币选择弹框 */
    fun showToTokenPicker() {
        _state.value = _state.value.copy(showToTokenPicker = true)
    }

    /** 关闭弹框 */
    fun dismissTokenPicker() {
        _state.value = _state.value.copy(showFromTokenPicker = false, showToTokenPicker = false)
    }

    /** 选择支付代币 */
    fun selectFromToken(token: SwapTokenInfo) {
        _state.value = _state.value.copy(
            fromToken = token,
            showFromTokenPicker = false,
            fromAmount = "",
            toAmount = "",
            fromBalance = if (token.symbol == "ETH") _state.value.fromBalance else "0.0"
        )
        if (token.symbol == "ETH") loadEthBalance()
        recalculate()
    }

    /** 选择接收代币 */
    fun selectToToken(token: SwapTokenInfo) {
        _state.value = _state.value.copy(
            toToken = token,
            showToTokenPicker = false
        )
        recalculate()
    }

    /**
     * 重新计算兑换金额
     */
    private fun recalculate() {
        val s = _state.value
        val fromAmount = s.fromAmount.toDoubleOrNull()

        if (fromAmount == null || fromAmount <= 0.0 || ethPrice <= 0.0) {
            _state.value = s.copy(
                toAmount = "",
                exchangeRate = "0.0",
                minimumReceived = "0.0",
                isValid = false,
                fromAmountError = null
            )
            return
        }

        // 简化汇率计算：基于 ETH 价格
        val rate = getExchangeRate(s.fromToken.symbol, s.toToken.symbol)
        val toAmount = BigDecimal(fromAmount).multiply(BigDecimal(rate)).setScale(6, RoundingMode.DOWN)
        val minimumReceived = toAmount.multiply(BigDecimal("0.995")).setScale(6, RoundingMode.DOWN)

        val balanceError = try {
            val bal = BigDecimal(s.fromBalance)
            if (BigDecimal(fromAmount) > bal) "余额不足" else null
        } catch (_: Exception) { null }

        _state.value = s.copy(
            toAmount = toAmount.stripTrailingZeros().toPlainString(),
            exchangeRate = BigDecimal(rate).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString(),
            minimumReceived = minimumReceived.stripTrailingZeros().toPlainString(),
            fromAmountError = balanceError,
            isValid = balanceError == null && fromAmount > 0
        )
    }

    /**
     * 获取两个代币之间的汇率
     * 基于 ETH 价格和稳定币 1:1 USD 的假设
     */
    private fun getExchangeRate(fromSymbol: String, toSymbol: String): Double {
        val stablecoins = setOf("USDT", "USDC", "DAI", "BUSD", "TUSD", "FRAX")

        val fromUsd = when {
            fromSymbol == "ETH" || fromSymbol == "WETH" -> ethPrice
            stablecoins.contains(fromSymbol) -> 1.0
            else -> 1.0 // 其他代币暂时按 1 USD
        }

        val toUsd = when {
            toSymbol == "ETH" || toSymbol == "WETH" -> ethPrice
            stablecoins.contains(toSymbol) -> 1.0
            else -> 1.0
        }

        return if (toUsd > 0) fromUsd / toUsd else 0.0
    }

    /** 执行兑换（模拟） */
    fun executeSwap() {
        if (!_state.value.isValid) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // TODO: 接入真实 DEX 合约
            kotlinx.coroutines.delay(2000)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "兑换功能开发中，敬请期待"
            )
        }
    }
}
