package com.mvvm.module_compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.signalFlow
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 助记词警告页面状态
 */
data class MnemonicWarningState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 助记词警告页面ViewModel
 * 负责创建钱包并生成助记词
 */
@HiltViewModel
@Deprecated("不用这个")
class MnemonicWarningViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletRepository: IWalletRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(MnemonicWarningState())
    val state: StateFlow<MnemonicWarningState> = _state.asStateFlow()
    
    /** 钱包创建成功事件流，携带助记词 */
    private val _walletCreatedEvent = MutableSharedFlow<String>()
    val walletCreatedEvent: SharedFlow<String> = _walletCreatedEvent.asSharedFlow()


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
        )

        /**
         * 创建钱包
         * 生成助记词并创建钱包文件
         */
        fun createWallet() {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)

                try {
                    // 准备钱包目录
                    val walletDir = File(context.filesDir, "wallets")
                    if (!walletDir.exists()) walletDir.mkdirs()

                    // 创建钱包请求
                    val request = CreateWalletRequest(
                        walletName = "我的钱包",
                        password = "temp_password", // 使用之前创建的密码
                        walletDir = walletDir
                    )

                    // 创建钱包
                    walletRepository.createWallet(request).collect { result ->
                        if (result != null) {
                            val mnemonic = result.mnemonic.joinToString(" ")
                            _walletCreatedEvent.emit(mnemonic)
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "创建钱包失败"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "创建钱包失败"
                    )
                }
            }
        }

    }}