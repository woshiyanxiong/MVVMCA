package com.mvvm.module_compose.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashState(
    val isLoading: Boolean = true,
    val hasWallet: Boolean = false,
    val shouldNavigate: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state

    fun checkWallet() {
        viewModelScope.launch {
            val walletList = walletRepository.getWalletList().first()
            val hasWallet = walletList.isNotEmpty()
            
            _state.value = _state.value.copy(
                isLoading = false,
                hasWallet = hasWallet,
                shouldNavigate = true
            )
        }
    }
}