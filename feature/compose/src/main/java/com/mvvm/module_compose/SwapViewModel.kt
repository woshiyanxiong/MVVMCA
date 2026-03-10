package com.mvvm.module_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IAccountRepository
import com.data.wallet.repo.ISwapRepository
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import com.mvvm.module_compose.TokenInfo
import javax.inject.Inject

/**
 * 兑换页面ViewModel
 * 负责管理代币兑换流程的状态和业务逻辑
 */
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val walletRepository: IWalletRepository,
    private val swapRepository: ISwapRepository,
    private val accountRepository: IAccountRepository
) : ViewModel() {
    
    /** 支付金额输入流 */
    private val _fromAmountUpdate = signalFlow<String>()
    /** 余额流 */
    private val _balanceFlow = signalFlow<BalanceInfo>()
    /** 代币交换标志流 */
    private val _swapTokensFlag = signalFlow<Boolean>()
    
    /** 兑换成功事件流 */
    private val _swapSuccessEvent = MutableSharedFlow<Unit>()
    val swapSuccessEvent: SharedFlow<Unit> = _swapSuccessEvent.asSharedFlow()
    
    /** 兑换错误事件流 */
    private val _swapErrorEvent = MutableSharedFlow<String>()
    val swapErrorEvent: SharedFlow<String> = _swapErrorEvent.asSharedFlow()
    
    init {
        // 初始化时加载余额
        loadBalances()
    }
    
    /**
     * 加载钱包余额
     */
    private fun loadBalances() {
        viewModelScope.launch {
            walletRepository.getMainWalletInfo().collect { info ->
                val ethBalance = info?.balance ?: "0.0"
                // TODO: 从合约获取 USDT 余额
                val usdtBalance = "0.0"
                _balanceFlow.emit(BalanceInfo(ethBalance, usdtBalance))
            }
        }
    }
    
    /**
     * 兑换页面的UI状态
     */
    val state: StateFlow<SwapState> = combine(
        _fromAmountUpdate.onStart { emit("") },
        _balanceFlow.onStart { emit(BalanceInfo("0.0", "0.0")) },
        _swapTokensFlag.onStart { emit(false) }
    ) { fromAmount, balances, isSwapped ->
        // 根据交换标志决定代币方向
        val tokenPair = if (isSwapped) {
            Pair(
                TokenInfo("USDT", "Tether USD", "0xdac17f958d2ee523a2206206994597c13d831ec7"),
                TokenInfo("ETH", "Ethereum", com.data.wallet.util.WeiConverter.ETH_ADDRESS)
            ) to Pair(balances.usdtBalance, balances.ethBalance)
        } else {
            Pair(
                TokenInfo("ETH", "Ethereum", com.data.wallet.util.WeiConverter.ETH_ADDRESS),
                TokenInfo("USDT", "Tether USD", "0xdac17f958d2ee523a2206206994597c13d831ec7")
            ) to Pair(balances.ethBalance, balances.usdtBalance)
        }
        val fromToken = tokenPair.first.first
        val toToken = tokenPair.first.second
        val fromBalance = tokenPair.second.first
        val toBalance = tokenPair.second.second
        
        // 计算兑换金额和相关信息
        val (toAmount, exchangeRate, minimumReceived, networkFee) = calculateSwapInfo(
            fromAmount, 
            fromToken.symbol, 
            toToken.symbol
        )
        
        val fromAmountError = validateAmount(fromAmount, fromBalance)
        val isValid = fromAmountError == null && fromAmount.isNotEmpty() && 
                     fromAmount.toDoubleOrNull() != null && fromAmount.toDouble() > 0
        
        SwapState(
            fromToken = fromToken,
            toToken = toToken,
            fromAmount = fromAmount,
            toAmount = toAmount,
            fromBalance = fromBalance,
            toBalance = toBalance,
            exchangeRate = exchangeRate,
            priceImpact = "< 0.01%",
            minimumReceived = minimumReceived,
            networkFee = networkFee,
            fromAmountError = fromAmountError,
            isValid = isValid,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SwapState())
    
    /**
     * 更新支付金额
     */
    fun updateFromAmount(amount: String) {
        _fromAmountUpdate.tryEmit(amount)
    }
    
    /**
     * 交换代币方向
     */
    fun swapTokens() {
        viewModelScope.launch {
            val currentFlag = _swapTokensFlag.replayCache.lastOrNull() ?: false
            _swapTokensFlag.emit(!currentFlag)
            // 清空输入金额
            _fromAmountUpdate.emit("")
        }
    }
    
    /**
     * 执行兑换
     */
    fun executeSwap() {
        val currentState = state.value
        if (!currentState.isValid) {
            return
        }
        
        viewModelScope.launch {
            try {
                // 从本地获取钱包密码
                val password = accountRepository.getWalletPassword().firstOrNull()
                if (password.isNullOrBlank()) {
                    _swapErrorEvent.emit("未找到钱包密码，请先设置密码")
                    return@launch
                }
                
                // 使用 SwapRepository 执行兑换
                swapRepository.executeSwap(
                    fromToken = currentState.fromToken.address,
                    toToken = currentState.toToken.address,
                    fromAmount = currentState.fromAmount,
                    minToAmount = currentState.minimumReceived,
                    password = password
                ).collect { txHash ->
                    if (txHash != null) {
                        // 兑换成功
                        _swapSuccessEvent.emit(Unit)
                    } else {
                        // 兑换失败
                        _swapErrorEvent.emit("兑换失败，请检查网络连接或稍后重试")
                    }
                }
            } catch (e: Exception) {
                _swapErrorEvent.emit("兑换异常: ${e.message}")
            }
        }
    }
    
    /**
     * 计算兑换信息
     * @return Pair(接收金额, 汇率, 最少接收, 网络费用)
     */
    private fun calculateSwapInfo(
        fromAmount: String,
        fromSymbol: String,
        toSymbol: String
    ): SwapInfo {
        if (fromAmount.isEmpty() || fromAmount.toDoubleOrNull() == null) {
            return SwapInfo("0.0", "0.0", "0.0", "0.0")
        }
        
        try {
            val amount = BigDecimal(fromAmount)
            
            // TODO: 从 DEX 或预言机获取实时汇率
            // 这里使用模拟汇率
            val rate = if (fromSymbol == "ETH") {
                BigDecimal("2000.0") // 1 ETH = 2000 USDT
            } else {
                BigDecimal("0.0005") // 1 USDT = 0.0005 ETH
            }
            
            val toAmount = amount.multiply(rate).setScale(6, RoundingMode.DOWN)
            
            // 计算滑点保护（0.5%）
            val slippageTolerance = BigDecimal("0.995")
            val minimumReceived = toAmount.multiply(slippageTolerance).setScale(6, RoundingMode.DOWN)
            
            // TODO: 从链上估算 gas 费用
            val networkFee = "0.002"
            
            return SwapInfo(
                toAmount = toAmount.toPlainString(),
                exchangeRate = rate.toPlainString(),
                minimumReceived = minimumReceived.toPlainString(),
                networkFee = networkFee
            )
        } catch (e: Exception) {
            return SwapInfo("0.0", "0.0", "0.0", "0.0")
        }
    }
    
    /**
     * 验证金额
     */
    private fun validateAmount(amount: String, balance: String): String? {
        return when {
            amount.isEmpty() -> null
            else -> {
                try {
                    val value = BigDecimal(amount)
                    val balanceValue = BigDecimal(balance)
                    when {
                        value <= BigDecimal.ZERO -> "金额必须大于0"
                        value > balanceValue -> "余额不足"
                        else -> null
                    }
                } catch (e: Exception) {
                    "金额格式错误"
                }
            }
        }
    }
}

/**
 * 余额信息
 */
data class BalanceInfo(
    val ethBalance: String,
    val usdtBalance: String
)

/**
 * 兑换信息
 */
data class SwapInfo(
    val toAmount: String,
    val exchangeRate: String,
    val minimumReceived: String,
    val networkFee: String
)
