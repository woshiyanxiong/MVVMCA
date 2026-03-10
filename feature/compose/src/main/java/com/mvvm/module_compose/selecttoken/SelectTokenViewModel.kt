package com.mvvm.module_compose.selecttoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.entity.TokenBalanceEntity
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

data class TokenItem(
    val name: String,
    val symbol: String,
    val balance: String,
    val usdValue: String = "",
    val logo: String? = null,
    val contractAddress: String?,
    val isNative: Boolean = false
)

data class SelectTokenState(
    val isLoading: Boolean = true,
    val tokens: List<TokenItem> = emptyList()
)

@HiltViewModel
class SelectTokenViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SelectTokenState())
    val state: StateFlow<SelectTokenState> = _state

    init {
        loadTokens()
    }

    private fun loadTokens() {
        viewModelScope.launch {
            _state.value = SelectTokenState(isLoading = true)

            val walletList = walletRepository.getWalletList().firstOrNull()
            val currentAddress = walletList?.firstOrNull() ?: ""

            val tokens = mutableListOf<TokenItem>()

            // 获取 ETH 价格
            val ethPrice = walletRepository.getEthPrice().firstOrNull() ?: 0.0

            // ETH 原生代币
            val ethBalance = walletRepository.getBalance(currentAddress).firstOrNull() ?: BigInteger.ZERO
            val ethBalanceDecimal = com.data.wallet.util.WeiConverter.weiToEth(ethBalance)
            val ethBalanceStr = ethBalanceDecimal.toPlainString()
            val ethUsdValue = ethBalanceDecimal.toDouble() * ethPrice

            tokens.add(
                TokenItem(
                    name = "Ethereum",
                    symbol = "ETH",
                    balance = ethBalanceStr,
                    usdValue = String.format(java.util.Locale.US, "$%.2f", ethUsdValue),
                    logo = null,
                    contractAddress = null,
                    isNative = true
                )
            )

            // ERC20 代币
            val erc20Tokens = walletRepository.getTokenBalances(currentAddress).firstOrNull() ?: emptyList()
            erc20Tokens.forEach { token ->
                val tokenUsdValue = estimateTokenUsdValue(token.symbol, token.balance)
                tokens.add(
                    TokenItem(
                        name = token.name,
                        symbol = token.symbol,
                        balance = token.balance,
                        usdValue = tokenUsdValue,
                        logo = token.logo,
                        contractAddress = token.contractAddress,
                        isNative = false
                    )
                )
            }

            _state.value = SelectTokenState(isLoading = false, tokens = tokens)
        }
    }

    /**
     * 估算代币的 USD 价值
     * 稳定币 (USDT, USDC, DAI, BUSD) 按 1:1 计算
     * 其他代币暂不显示 USD 价值
     */
    private fun estimateTokenUsdValue(symbol: String, balance: String): String {
        val stableCoins = setOf("USDT", "USDC", "DAI", "BUSD", "TUSD", "USDP", "FRAX")
        return if (symbol.uppercase() in stableCoins) {
            val value = balance.toDoubleOrNull() ?: 0.0
            String.format(java.util.Locale.US, "$%.2f", value)
        } else {
            ""
        }
    }
}
