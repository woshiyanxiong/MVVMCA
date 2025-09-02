package com.mvvm.module_compose.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IAccountRepository
import com.mvvm.module_compose.CreatePasswordState
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
import javax.inject.Inject

/**
 * 创建钱包密码的ViewModel
 * 负责管理密码创建流程的状态和业务逻辑
 */
@HiltViewModel
class CreatePasswordViewModel @Inject constructor(
    private val accountRepository: IAccountRepository
) : ViewModel() {

    /** 密码输入流 */
    private val _passwordUpdate = signalFlow<String>()

    /** 确认密码输入流 */
    private val _confirmPasswordUpdate = signalFlow<String>()

    /** 用户协议同意状态流 */
    private val _agreementUpdate = signalFlow<Boolean>()

    /** 密码创建成功事件流 */
    private val _passwordCreatedEvent = MutableSharedFlow<Unit>()
    val passwordCreatedEvent: SharedFlow<Unit> = _passwordCreatedEvent.asSharedFlow()

    /**
     * 创建密码页面的UI状态
     * 组合密码、确认密码和协议同意状态，实时验证并更新UI状态
     */
    val state: StateFlow<CreatePasswordState> = combine(
        _passwordUpdate.onStart { emit("") },
        _confirmPasswordUpdate.onStart { emit("") }) { password, confirmPassword ->
        val passwordError = validatePassword(password)
        val confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        val isValid = passwordError == null && confirmPasswordError == null &&
                password.isNotEmpty() && confirmPassword.isNotEmpty()

        CreatePasswordState(
            password = password,
            confirmPassword = confirmPassword,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            isValid = isValid,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.Lazily, CreatePasswordState())

    /**
     * 更新密码输入
     * @param password 用户输入的密码
     */
    fun updatePassword(password: String) {
        _passwordUpdate.tryEmit(password)
    }

    /**
     * 更新确认密码输入
     * @param confirmPassword 用户输入的确认密码
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _confirmPasswordUpdate.tryEmit(confirmPassword)
    }


    /**
     * 创建钱包密码
     * 验证当前状态有效性后，将密码保存到本地存储
     */
    fun createPassword() {
        val currentState = state.value
        if (currentState.isValid) {
            viewModelScope.launch {
                accountRepository.saveWalletPassword(currentState.password)
                    .collect { success ->
                        if (success) {
                            // 发送密码创建成功事件
                            _passwordCreatedEvent.emit(Unit)
                        }
                    }
            }
        }
    }

    /**
     * 验证密码强度
     * @param password 待验证的密码
     * @return 错误信息，null表示验证通过
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> null
            password.length < 8 -> "密码至少需要8个字符"
            !password.any { it.isUpperCase() } -> "密码需要包含大写字母"
            !password.any { it.isLowerCase() } -> "密码需要包含小写字母"
            !password.any { it.isDigit() } -> "密码需要包含数字"
            else -> null
        }
    }

    /**
     * 验证确认密码是否与原密码一致
     * @param password 原密码
     * @param confirmPassword 确认密码
     * @return 错误信息，null表示验证通过
     */
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> null
            password != confirmPassword -> "两次输入的密码不一致"
            else -> null
        }
    }
}