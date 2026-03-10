package com.mvvm.module_compose.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val logoUrl: String? = null,
    val balance: String = ""
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
    /** 缓存代币余额 map: symbol -> balance */
    private var tokenBalanceMap: Map<String, String> = emptyMap()

    init {
        loadBalances()
        loadTokenList()
    }

    /**
     * 从 Repository 获取所有代币余额（ETH + ERC20），通过合约地址匹配
     */
    private fun loadBalances() {
        viewModelScope.launch {
            // 获取 ETH 价格
            walletRepository.getEthPrice().firstOrNull()?.let { price ->
                ethPrice = price
            }

            // 获取所有代币余额 (合约地址 -> 余额)
            walletRepository.getAllTokenBalances().collect { balances ->
                tokenBalanceMap = balances
                ethBalance = balances["0x0000000000000000000000000000000000000000"] ?: "0.0"

                // 更新 tokenList 中的余额
                val updatedList = _state.value.tokenList.map { token ->
                    token.copy(balance = balances[token.address.lowercase()] ?: "")
                }
                val currentFromBalance = balances[_state.value.fromToken.address.lowercase()] ?: "0.0"
                _state.value = _state.value.copy(
                    fromBalance = currentFromBalance,
                    tokenList = updatedList
                )
                recalculate()
            }
        }
    }

    /**
     * 从 Repository 获取 Uniswap 主网热门代币列表
     */
    private fun loadTokenList() {
        viewModelScope.launch {
            walletRepository.getUniswapTokenList().collect { tokens ->
                val ethToken = SwapTokenInfo("ETH", "Ethereum", "0x0000000000000000000000000000000000000000", 18, null,
                    tokenBalanceMap["0x0000000000000000000000000000000000000000"] ?: "")
                val tokenInfoList = listOf(ethToken) + tokens.map { token ->
                    SwapTokenInfo(
                        symbol = token.symbol,
                        name = token.name,
                        address = token.address,
                        decimals = token.decimals,
                        logoUrl = token.logoURI,
                        balance = tokenBalanceMap[token.address.lowercase()] ?: ""
                    )
                }
                _state.value = _state.value.copy(
                    tokenList = tokenInfoList,
                    isLoadingTokens = false,
                    toToken = tokenInfoList.find { it.symbol == _state.value.toToken.symbol } ?: _state.value.toToken
                )
            }
        }
    }

    fun updateFromAmount(amount: String) {
        _state.value = _state.value.copy(fromAmount = amount)
        recalculate()
    }

    fun swapTokens() {
        val s = _state.value
        _state.value = s.copy(
            fromToken = s.toToken,
            toToken = s.fromToken,
            fromAmount = "",
            toAmount = "",
            fromBalance = s.toToken.balance.ifEmpty { "0.0" },
            fromAmountError = null
        )
        recalculate()
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
        recalculate()
    }

    fun selectToToken(token: SwapTokenInfo) {
        _state.value = _state.value.copy(
            toToken = token,
            showToTokenPicker = false
        )
        recalculate()
    }

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

    private fun getExchangeRate(fromSymbol: String, toSymbol: String): Double {
        val stablecoins = setOf("USDT", "USDC", "DAI", "BUSD", "TUSD", "FRAX")
        val fromUsd = when {
            fromSymbol == "ETH" || fromSymbol == "WETH" -> ethPrice
            stablecoins.contains(fromSymbol) -> 1.0
            else -> 1.0
        }
        val toUsd = when {
            toSymbol == "ETH" || toSymbol == "WETH" -> ethPrice
            stablecoins.contains(toSymbol) -> 1.0
            else -> 1.0
        }
        return if (toUsd > 0) fromUsd / toUsd else 0.0
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
