package com.data.wallet.domain

import android.content.Context
import com.data.wallet.model.CreateWalletRequest
import com.data.wallet.repo.IAccountRepository
import com.data.wallet.repo.IWalletRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import javax.inject.Inject

/**
 *create by 2025/8/31
 *@author yx
 */
class CreateAccountAndWalletCause @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: IWalletRepository,
    private val accountRepository: IAccountRepository
) {
    private val defaultAccountName = "account1"
    
    suspend operator fun invoke(accountName: String?): Boolean {
        val account = accountRepository.createAccount(accountName ?: defaultAccountName).firstOrNull()
        val walletDir = File(context.filesDir, "wallets")
        if (!walletDir.exists()) walletDir.mkdirs()
        return account != null
    }
}