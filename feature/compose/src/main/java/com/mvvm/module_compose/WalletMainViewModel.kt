package com.mvvm.module_compose

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IWalletRepository
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger

data class WalletMainState(
    val isLoading: Boolean = false,
    val walletAddress: String = "",
    val ethBalance: String = "0.0",
    val ethValue: String = "$0.00",
    val walletList: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WalletMainViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WalletMainState())
    val state: StateFlow<WalletMainState> = _state

    fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // 获取钱包列表
                val walletList = walletRepository.getWalletList().first()
                
                if (walletList.isNotEmpty()) {
                    val currentAddress = walletList.first()
                    LogUtils.e("当前地址: $currentAddress")
                    // 获取ETH余额
                    val balance = walletRepository.getBalance(currentAddress).firstOrNull()?: BigInteger("0")
                    val ethBalance = convertWeiToEth(balance)
                    
                    // 简化的价格计算（实际应该调用价格API）
                    val ethPrice = 2000.0 // 假设ETH价格
                    val ethValue = String.format("$%.2f", ethBalance.toDouble() * ethPrice)
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        walletAddress = formatAddress(currentAddress),
                        ethBalance = String.format("%.4f", ethBalance),
                        ethValue = ethValue,
                        walletList = walletList
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "未找到钱包"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载钱包数据失败: ${e.message}"
                )
            }
        }
    }
    
    private fun convertWeiToEth(wei: BigInteger): BigDecimal {
        val weiInEth = BigDecimal("1000000000000000000") // 10^18
        return BigDecimal(wei).divide(weiInEth)
    }
    
    private fun formatAddress(address: String): String {
        return if (address.length > 10) {
            "${address.substring(0, 6)}...${address.substring(address.length - 4)}"
        } else {
            address
        }
    }
}