package com.mvvm.module_compose.receive

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.wallet.repo.IWalletRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ReceiveState(
    val isLoading: Boolean = true,
    val walletAddress: String = "",
    val qrCodeBitmap: Bitmap? = null,
    val copied: Boolean = false
)

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val walletRepository: IWalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReceiveState())
    val state: StateFlow<ReceiveState> = _state

    init {
        loadAddress()
    }

    private fun loadAddress() {
        viewModelScope.launch {
            val address = walletRepository.getWalletList().firstOrNull()?.firstOrNull() ?: ""
            val qrBitmap = generateQrCode("ethereum:$address", 512)
            _state.value = ReceiveState(
                isLoading = false,
                walletAddress = address,
                qrCodeBitmap = qrBitmap
            )
        }
    }

    fun onCopied() {
        _state.value = _state.value.copy(copied = true)
    }

    /**
     * 使用 ZXing 生成二维码
     * @param content 二维码内容 (EIP-681 格式: ethereum:0x地址)
     * @param size 二维码尺寸 (像素)
     */
    private suspend fun generateQrCode(content: String, size: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val hints = mapOf(
                    EncodeHintType.MARGIN to 1,
                    EncodeHintType.CHARACTER_SET to "UTF-8"
                )
                val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
}
