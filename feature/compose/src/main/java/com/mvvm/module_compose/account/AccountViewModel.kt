package com.mvvm.module_compose.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 账户信息
 */
data class AccountInfo(
    val address: String,
    val name: String,
    val balance: String = "0.0",
    val privateKey: String = ""
)

/**
 * 账户页面状态
 */
data class AccountState(
    val accounts: List<AccountInfo> = emptyList(),
    val currentAccount: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: IAccountRepository
) : ViewModel() {
    
    private val _loadTrigger = signalFlow<Boolean>()
    
    val state: StateFlow<AccountState> = _loadTrigger.flatMapLatest {
        combine(
            accountRepository.getAccountList(),
            accountRepository.getCurrentAccount()
        ) { accounts, currentAccount ->
            val accountInfos = accounts.map { account ->
                AccountInfo(
                    address = account.address,
                    name = account.name,
                    balance = formatBalance(account.balance.toString())
                )
            }
            AccountState(
                accounts = accountInfos,
                currentAccount = currentAccount?.address ?: "",
                isLoading = false
            )
        }
    }.onStart {
        emit(AccountState(isLoading = true))
    }.stateIn(viewModelScope, SharingStarted.Lazily, AccountState())
    
    init {
        loadAccounts()
    }
    
    /**
     * 加载账户列表
     */
    fun loadAccounts() {
        _loadTrigger.tryEmit(true)
    }
    
    /**
     * 创建新账户
     */
    fun createAccount() {
        _createTrigger.tryEmit("账户 ${(state.value.accounts.size + 1)}")
    }
    
    /**
     * 导入账户
     */
    fun importAccount() {
        // TODO: 实现导入账户功能
    }
    
    /**
     * 选择账户
     */
    fun selectAccount(address: String) {
        _selectTrigger.tryEmit(address)
    }
    
    /**
     * 删除账户
     */
    fun deleteAccount(address: String) {
        _deleteTrigger.tryEmit(address)
    }
    
    private val _createTrigger = signalFlow<String>()
    private val _selectTrigger = signalFlow<String>()
    private val _deleteTrigger = signalFlow<String>()
    
    init {
        loadAccounts()
        
        // 创建账户
        _createTrigger.flatMapLatest { name ->
            accountRepository.createAccount(name)
        }.onEach {
            loadAccounts()
        }.launchIn(viewModelScope)
        
        // 切换账户
        _selectTrigger.flatMapLatest { address ->
            accountRepository.switchAccount(address)
        }.onEach {
            loadAccounts()
        }.launchIn(viewModelScope)
        
        // 删除账户
        _deleteTrigger.flatMapLatest { address ->
            accountRepository.deleteAccount(address)
        }.onEach {
            loadAccounts()
        }.launchIn(viewModelScope)
    }
    
    /**
     * 格式化余额显示
     */
    private fun formatBalance(balance: String): String {
        return try {
            val value = BigDecimal(balance)
            val ethValue = value.divide(BigDecimal("1000000000000000000"))
            String.format("%.6f", ethValue)
        } catch (e: Exception) {
            "0.000000"
        }
    }
}