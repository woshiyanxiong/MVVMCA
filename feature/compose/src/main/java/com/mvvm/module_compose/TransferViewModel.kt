package com.mvvm.module_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
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
    private val walletRepository: IWalletRepository
) : ViewModel() {
    
    /** 收款地址输入流 */
    private val _addressUpdate = signalFlow<String>()
    /** 转账金额输入流 */
    private val _amountUpdate = signalFlow<String>()
    
    /** 转账成功事件流 */
    private val _transferSuccessEvent = MutableSharedFlow<Unit>()
    val transferSuccessEvent: SharedFlow<Unit> = _transferSuccessEvent.asSharedFlow()
    
    /**
     * 转账页面的UI状态
     * 组合地址、金额输入，实时验证并更新UI状态
     */
    val state: StateFlow<TransferState> = combine(
        _addressUpdate.onStart { emit("") },
        _amountUpdate.onStart { emit("") }
    ) { address, amount ->
        val addressError = validateAddress(address)
        val amountError = validateAmount(amount)
        val isValid = addressError == null && amountError == null && 
                     address.isNotEmpty() && amount.isNotEmpty()
        
        TransferState(
            toAddress = address,
            amount = amount,
            balance = "1.5", // TODO: 从钱包获取实际余额
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
        if (currentState.isValid) {
            viewModelScope.launch {
//                walletRepository.sendTransaction(
//                    toAddress = currentState.toAddress,
//                    amount = currentState.amount
//                ).collect { txHash ->
//                    if (txHash != null) {
//                        _transferSuccessEvent.emit(Unit)
//                    } else {
//                        // TODO: 处理转账失败
//                    }
//                }
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
     * @return 错误信息，null表示验证通过
     */
    private fun validateAmount(amount: String): String? {
        return when {
            amount.isEmpty() -> null
            else -> {
                try {
                    val value = BigDecimal(amount)
                    when {
                        value <= BigDecimal.ZERO -> "金额必须大于0"
                        value > BigDecimal("1.5") -> "余额不足" // TODO: 使用实际余额
                        else -> null
                    }
                } catch (e: Exception) {
                    "金额格式错误"
                }
            }
        }
    }
}