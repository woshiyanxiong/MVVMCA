package com.data.wallet.entity

import com.data.wallet.model.TransactionModel
import java.math.BigDecimal
import java.math.BigInteger

data class MainWalletInfoEntity(
    val currentAddress: String,
    val walletList: List<String>,
    val balance: String,
    val ethValue: String,
    val totalValue: String,
    val tokenBalances: List<TokenBalanceEntity>,
    val transaction: List<TransactionModel>
)