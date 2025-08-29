package com.mvvm.module_compose.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject

data class WalletCreationState(
    val isLoading: Boolean = false,
    val mnemonic: List<String> = emptyList(),
    val walletAddress: String = "",
    val error: String? = null,
    val isCreated: Boolean = false
)

@HiltViewModel
class WalletCreationViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WalletCreationState())
    val state: StateFlow<WalletCreationState> = _state
    private val test = "[fatal, stand, various, brisk, aisle, object, proof, skull, else, runway, jaguar, unique]"
    private val _createWallet = signalFlow<CreateWalletRequest>()
    private val createWalletResult = _createWallet.flatMapLatest {
        walletRepository.createWallet(it)
    }.onEach { result ->
        if (result == null) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "创建钱包失败"
            )
            return@onEach
        }
        _state.value = _state.value.copy(
            isLoading = false,
            mnemonic = result.mnemonic,
            walletAddress = result.walletAddress,
            isCreated = true
        )
    }

    fun createWallet(walletName: String, password: String, walletDir: File) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        Log.e("判断仓库", "${walletRepository != null}")
        _createWallet.tryEmit(CreateWalletRequest(walletName, password, walletDir))
    }

    init {
        createWalletResult.launchIn(viewModelScope)
    }

}