package com.mvvm.module_compose.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashState(
    val isLoading: Boolean = true,
    val hasPassword: Boolean = false,
    val shouldNavigate: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountRepository: IAccountRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state

    fun checkPassword() {
        viewModelScope.launch {
            // 检查是否存在本地密码
            val password = accountRepository.getWalletPassword().first()
            val hasPassword = !password.isNullOrBlank()
            
            _state.value = _state.value.copy(
                isLoading = false,
                hasPassword = hasPassword,
                shouldNavigate = true
            )
        }
    }
}