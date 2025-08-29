package com.mvvm.module_compose.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class WalletImportState(
    val mnemonic: String = "",
    val password: String = "",
    val walletName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val mnemonicError: String? = null,
    val passwordError: String? = null,
    val canImport: Boolean = false
)

@HiltViewModel
class WalletImportViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WalletImportState())
    val state: StateFlow<WalletImportState> = _state.asStateFlow()
    
    fun updateMnemonic(mnemonic: String) {
        _state.value = _state.value.copy(
            mnemonic = mnemonic,
            mnemonicError = validateMnemonic(mnemonic),
            error = null
        )
        updateCanImport()
    }
    
    fun updatePassword(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = validatePassword(password),
            error = null
        )
        updateCanImport()
    }
    
    fun updateWalletName(name: String) {
        _state.value = _state.value.copy(
            walletName = name,
            error = null
        )
        updateCanImport()
    }
    
    fun importWallet() {
        val currentState = _state.value
        if (!currentState.canImport) return
        
        _state.value = currentState.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val request = ImportWalletRequest(
                    mnemonic = currentState.mnemonic.trim().split("\\s+".toRegex()),
                    password = currentState.password,
                    walletName = currentState.walletName.ifEmpty { "我的钱包" },
                    walletDir = File("/data/data/com.mvvm.module_compose/files/wallets")
                )
                
                walletRepository.importWalletFromMnemonic(request).collect { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }
    
    private fun updateCanImport() {
        val currentState = _state.value
        _state.value = currentState.copy(
            canImport = currentState.mnemonic.isNotBlank() &&
                    currentState.password.isNotBlank() &&
                    currentState.mnemonicError == null &&
                    currentState.passwordError == null
        )
    }
    
    private fun validateMnemonic(mnemonic: String): String? {
        val words = mnemonic.trim().split("\\s+".toRegex())
        return when {
            mnemonic.isBlank() -> null
            words.size != 12 -> "助记词必须是12个单词"
            words.any { it.isBlank() } -> "助记词不能包含空白"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> null
            password.length < 6 -> "密码至少6位"
            else -> null
        }
    }
}