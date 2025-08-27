package com.mvvm.module_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.model.TransactionModel
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale
import javax.inject.Inject

data class WalletMainState(
    val isLoading: Boolean = false,
    val walletAddress: String = "",
    val ethBalance: String = "0.0",
    val ethValue: String = "$0.00",
    val walletList: List<String> = emptyList(),
    val transactions: List<TransactionModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WalletMainViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    private val _getInfo = signalFlow<Boolean>()
    val state: StateFlow<WalletMainState> = _getInfo.flatMapLatest {
        walletRepository.getMainWalletInfo()
    }.mapNotNull { entity ->
        val ethBalance = convertWeiToEth(entity?.balance)
        WalletMainState(
            isLoading = false,
            walletAddress = formatAddress(entity?.currentAddress),
            ethBalance = String.format(Locale.US, "%.4f", ethBalance),
            ethValue = entity?.ethValue ?: "$0.00",
            walletList = entity?.walletList ?: emptyList(),
            transactions = entity?.transaction ?: emptyList()
        )
    }.onStart {
        WalletMainState(isLoading = true, error = null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, WalletMainState())


    fun loadWalletData() {
        _getInfo.tryEmit(true)
    }

    private fun convertWeiToEth(wei: BigInteger?): BigDecimal {
        val weiInEth = BigDecimal("1000000000000000000") // 10^18
        return BigDecimal(wei).divide(weiInEth)
    }

    private fun formatAddress(address: String?): String {
        if (address.isNullOrEmpty()) return ""
        return if (address.length > 10) {
            "${address.substring(0, 6)}...${address.substring(address.length - 4)}"
        } else {
            address
        }
    }
}