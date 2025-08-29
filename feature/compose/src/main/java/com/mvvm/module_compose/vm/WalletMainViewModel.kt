package com.mvvm.module_compose.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.repo.IWalletRepository
import com.mvvm.module_compose.convertWeiToEth
import com.mvvm.module_compose.formatTime
import com.mvvm.module_compose.uistate.TransactionUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class WalletMainState(
    val isLoading: Boolean = false,
    val walletAddress: String = "",
    val ethBalance: String = "0.0",
    val ethValue: String = "$0.00",
    val walletList: List<String> = emptyList(),
    val transactions: List<TransactionUIState> = emptyList(),
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
        WalletMainState(
            isLoading = false,
            walletAddress = formatAddress(entity?.currentAddress),
            ethBalance = entity?.balance?: "0.00",
            ethValue = entity?.ethValue ?: "$0.00",
            walletList = entity?.walletList ?: emptyList(),
            transactions = entity?.transaction?.map { transaction->
                TransactionUIState(
                    isReceive = transaction.isReceive,
                    amount = convertWeiToEth(transaction.value),
                    symbol = "ETH",
                    address = formatAddress(if (transaction.isReceive) transaction.from else transaction.to),
                    time = formatTime(transaction.timestamp)
                )
            }?: emptyList()
        )
    }.onStart {
        emit(WalletMainState(isLoading = true, error = null))
    }.stateIn(viewModelScope, SharingStarted.Lazily, WalletMainState())


    fun loadWalletData() {
        _getInfo.tryEmit(true)
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