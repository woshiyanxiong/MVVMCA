package com.mvvm.module_compose.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 钱包登录页面状态
 */
data class AccountState(
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: IAccountRepository
) : ViewModel() {
    
    private val _passwordUpdate = signalFlow<String>()

    /** 登录成功事件流 */
    private val _loginSuccessEvent = signalFlow<Unit>()
    val loginSuccessEvent: SharedFlow<Unit> = _loginSuccessEvent.asSharedFlow()

    private var pwd = ""
    private val _uiState =MutableStateFlow( AccountState(
        password = "",
        isLoading = false
    ))
    val state: StateFlow<AccountState> = _uiState.asStateFlow()


    private val _loginEvent = signalFlow<Boolean>()
    private val login = _loginEvent.flatMapLatest {
        delay(300)
        accountRepository.verifyWalletPassword(pwd)
    }.onEach {

        if (it){
            _loginSuccessEvent.tryEmit(Unit)
        }
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
    
    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        this.pwd = password
    }
    
    /**
     * 登录
     */
    fun login() {
        if (!pwd.isBlank()){
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginEvent.tryEmit(true)
        }
    }
    
    init {
        login.launchIn(viewModelScope)
    }
}