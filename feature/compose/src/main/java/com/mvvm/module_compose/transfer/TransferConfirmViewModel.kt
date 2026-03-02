package com.mvvm.module_compose.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IAccountRepository
import com.data.wallet.repo.IWalletRepository
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale
import javax.inject.Inject

data class TransferConfirmState(
    val toAddress: String = "",
    val fromAddress: String = "",
    val amount: String = "0",
    val fiatValue: String = "$0.00",
    val networkFee: String = "0.000001 ETH (< $0.01)",
    val showPasswordDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class TransferConfirmViewModel @Inject constructor(
    private val walletRepository: IWalletRepository,
    private val accountRepository: IAccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransferConfirmState())
    val state: StateFlow<TransferConfirmState> = _state.asStateFlow()

    fun init(toAddress: String, amount: String, balance: String) {
        viewModelScope.launch {
            val fromAddress = walletRepository.getWalletList().firstOrNull()?.firstOrNull() ?: ""
            val ethPrice = 2000.0
            val amountDouble = amount.toDoubleOrNull() ?: 0.0
            val fiatValue = String.format(Locale.US, "$%.2f", amountDouble * ethPrice)

            _state.value = _state.value.copy(
                toAddress = toAddress,
                fromAddress = fromAddress,
                amount = amount,
                fiatValue = fiatValue
            )
        }
    }

    fun showPasswordDialog() {
        _state.value = _state.value.copy(showPasswordDialog = true, passwordError = null)
    }

    fun hidePasswordDialog() {
        _state.value = _state.value.copy(showPasswordDialog = false, passwordError = null)
    }

    fun executeTransfer(password: String) {
        val currentState = _state.value
        _state.value = currentState.copy(isLoading = true, passwordError = null, error = null)

        viewModelScope.launch {
            // 先验证密码
            val isValid = accountRepository.verifyWalletPassword(password).firstOrNull() ?: false
            if (!isValid) {
                _state.value = _state.value.copy(isLoading = false, passwordError = "密码错误")
                return@launch
            }

            walletRepository.sendTransaction(
                toAddress = currentState.toAddress,
                amount = currentState.amount,
                password = password
            ).catch { e ->
                LogUtils.e("TransferConfirm", "转账异常: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    showPasswordDialog = false,
                    error = "转账失败: ${e.message}"
                )
            }.collect { txHash ->
                if (txHash != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showPasswordDialog = false,
                        isSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showPasswordDialog = false,
                        error = "转账失败，请检查网络连接"
                    )
                }
            }
        }
    }
}
