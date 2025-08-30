package com.mvvm.module_compose.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 钱包设置页面状态
 * @param walletAddress 钱包地址
 * @param isLoading 是否正在加载
 * @param error 错误信息
 */
data class WalletSettingsState(
    val walletAddress: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletSettingsViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    
    private val _loadTrigger = signalFlow<Boolean>()
    
    val state: StateFlow<WalletSettingsState> = _loadTrigger.flatMapLatest {
        walletRepository.getWalletList()
    }.mapNotNull { walletList ->
        WalletSettingsState(
            walletAddress = formatAddress(walletList.firstOrNull()),
            isLoading = false
        )
    }.onStart {
        emit(WalletSettingsState(isLoading = true))
    }.stateIn(viewModelScope, SharingStarted.Lazily, WalletSettingsState())
    
    init {
        loadWalletInfo()
    }
    
    /**
     * 加载钱包信息
     */
    fun loadWalletInfo() {
        _loadTrigger.tryEmit(true)
    }
    
    /**
     * 备份助记词
     */
    fun backupMnemonic() {
        // TODO: 实现备份助记词功能
    }
    
    /**
     * 修改密码
     */
    fun changePassword() {
        // TODO: 实现修改密码功能
    }
    
    /**
     * 删除钱包
     */
    fun deleteWallet() {
        // TODO: 实现删除钱包功能
    }
    
    /**
     * 格式化地址显示
     */
    private fun formatAddress(address: String?): String {
        if (address.isNullOrEmpty()) return ""
        return if (address.length > 10) {
            "${address.substring(0, 6)}...${address.substring(address.length - 4)}"
        } else {
            address
        }
    }
}