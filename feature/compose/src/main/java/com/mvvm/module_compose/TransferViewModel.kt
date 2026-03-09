package com.mvvm.module_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IAccountRepository
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
import javax.inject.Inject

/**
 * 转账页面ViewModel
 * 负责管理转账流程的状态和业务逻辑
 */
@HiltViewModel
class TransferViewModel @Inject constructor(
    private val walletRepository: IWalletRepository,
    private val accountRepository: IAccountRepository
) : ViewModel() {
    
    /** 收款地址输入流 */
    private val _addressUpdate = signalFlow<String>()
    /** 转账金额输入流 */
    private val _amountUpdate = signalFlow<String>()
    /** 实际余额流 */
    private val _balanceFlow = signalFlow<String>()
    /** 币种符号 */
    private var _tokenSymbol = "ETH"
    
    /** 转账成功事件流 */
    private val _transferSuccessEvent = MutableSharedFlow<Unit>()
    val transferSuccessEvent: SharedFlow<Unit> = _transferSuccessEvent.asSharedFlow()
    
    /** 转账错误事件流 */
    private val _transferErrorEvent = MutableSharedFlow<String>()
    val transferErrorEvent: SharedFlow<String> = _transferErrorEvent.asSharedFlow()
    
    init {
        // 初始化时加载余额
        loadBalance()
    }
    
    /**
     * 设置选中的币种信息
     * @param symbol 币种符号
     * @param balance 币种余额
     */
    fun setTokenInfo(symbol: String, balance: String) {
        _tokenSymbol = symbol
        _balanceFlow.tryEmit(balance)
    }
    
    /**
     * 加载钱包余额
     */
    private fun loadBalance() {
        viewModelScope.launch {
            walletRepository.getMainWalletInfo().collect { info ->
                info?.balance?.let { balance ->
                    _balanceFlow.emit(balance)
                }
            }
        }
    }
    
    /**
     * 转账页面的UI状态
     * 组合地址、金额输入，实时验证并更新UI状态
     */
    val state: StateFlow<TransferState> = combine(
        _addressUpdate.onStart { emit("") },
        _amountUpdate.onStart { emit("") },
        _balanceFlow.onStart { emit("0.0") }
    ) { address, amount, balance ->
        val addressError = validateAddress(address)
        val amountError = validateAmount(amount, balance)
        val isValid = addressError == null && amountError == null && 
                     address.isNotEmpty() && amount.isNotEmpty()
        
        TransferState(
            toAddress = address,
            amount = amount,
            balance = balance,
            tokenSymbol = _tokenSymbol,
            addressError = addressError,
            amountError = amountError,
            isValid = isValid,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, TransferState())
    
    /**
     * 更新收款地址
     * @param address 用户输入的收款地址
     */
    fun updateAddress(address: String) {
        _addressUpdate.tryEmit(address)
    }
    
    /**
     * 更新转账金额
     * @param amount 用户输入的转账金额
     */
    fun updateAmount(amount: String) {
        _amountUpdate.tryEmit(amount)
    }
    
    /**
     * 执行转账
     * 验证当前状态有效性后，执行转账操作
     */
    fun transfer() {
        val currentState = state.value
        if (!currentState.isValid) {
            return
        }
        
        viewModelScope.launch {
            try {
                // 从本地获取钱包密码
                val password = accountRepository.getWalletPassword().firstOrNull()
                if (password.isNullOrBlank()) {
                    _transferErrorEvent.emit("未找到钱包密码，请先设置密码")
                    return@launch
                }
                
                walletRepository.sendTransaction(
                    toAddress = currentState.toAddress,
                    amount = currentState.amount,
                    password = password
                ).collect { txHash ->
                    if (txHash != null) {
                        // 转账成功
                        _transferSuccessEvent.emit(Unit)
                    } else {
                        // 转账失败
                        _transferErrorEvent.emit("转账失败，请检查网络连接或稍后重试")
                    }
                }
            } catch (e: Exception) {
                _transferErrorEvent.emit("转账异常: ${e.message}")
            }
        }
    }
    
    /**
     * 验证收款地址格式
     * @param address 待验证的地址
     * @return 错误信息，null表示验证通过
     */
    private fun validateAddress(address: String): String? {
        return when {
            address.isEmpty() -> null
            !address.startsWith("0x") -> "地址格式错误"
            address.length != 42 -> "地址长度不正确"
            !address.matches(Regex("^0x[a-fA-F0-9]{40}$")) -> "地址包含无效字符"
            else -> null
        }
    }
    
    /**
     * 验证转账金额
     * @param amount 待验证的金额
     * @param balance 当前余额
     * @return 错误信息，null表示验证通过
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