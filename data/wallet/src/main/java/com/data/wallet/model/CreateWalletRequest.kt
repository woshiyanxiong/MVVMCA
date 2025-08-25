package com.data.wallet.model

import java.io.File

data class CreateWalletRequest(
    val walletName: String,
    val password: String,
    val walletDir: File
)

data class ImportWalletRequest(
    val mnemonic: List<String>,
    val password: String,
    val walletDir: File,
    val walletName: String = "导入的钱包"
)