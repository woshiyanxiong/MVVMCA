package com.mvvm.module_compose.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.model.ImportWalletRequest
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 钱包导入页面状态
 * @param mnemonic 助记词
 * @param password 钱包密码
 * @param walletName 钱包名称
 * @param isLoading 是否正在导入
 * @param isSuccess 是否导入成功
 * @param error 错误信息
 * @param mnemonicError 助记词验证错误
 * @param passwordError 密码验证错误
 * @param canImport 是否可以导入
 */
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
    private val walletRepository: IWalletRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(WalletImportState())
    val state: StateFlow<WalletImportState> = _state.asStateFlow()
    
    /**
     * 更新助记词
     */
    fun updateMnemonic(mnemonic: String) {
        _state.value = _state.value.copy(
            mnemonic = mnemonic,
            mnemonicError = validateMnemonic(mnemonic),
            error = null,
            canImport = canImport(mnemonic, _state.value.password)
        )
    }
    
    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = validatePassword(password),
            error = null,
            canImport = canImport(_state.value.mnemonic, password)
        )
    }
    
    /**
     * 更新钱包名称
     */
    fun updateWalletName(name: String) {
        _state.value = _state.value.copy(
            walletName = name,
            error = null
        )
    }
    
    /**
     * 导入钱包
     */
    fun importWallet() {
        val currentState = _state.value
        if (!currentState.canImport) return
        
        _state.value = currentState.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            val request = ImportWalletRequest(
                mnemonic = currentState.mnemonic.trim().split("\\s+".toRegex()),
                password = currentState.password,
                walletName = currentState.walletName.ifEmpty { "我的钱包" },
                walletDir = File(context.filesDir, "wallets")
            )
            
            walletRepository.importWalletFromMnemonic(request)
                .catch { 
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "导入失败: ${it.message}"
                    )
                }
                .collect {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
        }
    }
    
    /**
     * 检查是否可以导入
     */
    private fun canImport(mnemonic: String, password: String): Boolean {
        return mnemonic.isNotBlank() &&
                password.isNotBlank() &&
                validateMnemonic(mnemonic) == null &&
                validatePassword(password) == null
    }
    
    /**
     * 验证助记词格式
     */
    private fun validateMnemonic(mnemonic: String): String? {
        val words = mnemonic.trim().split("\\s+".toRegex())
        return when {
            mnemonic.isBlank() -> null
            words.size != 12 -> "助记词必须是12个单词"
            words.any { it.isBlank() } -> "助记词不能包含空白"
            else -> null
        }
    }
    
    /**
     * 验证密码格式
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> null
            password.length < 6 -> "密码至少6位"
            else -> null
        }
    }
}