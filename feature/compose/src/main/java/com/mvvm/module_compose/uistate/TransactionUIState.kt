package com.mvvm.module_compose.uistate

data class TransactionUIState(
    val isReceive: Boolean, // "send" or "receive"
    val amount: String,
    val symbol: String,
    val address: String,
    val time: String,
    val status: String = "success"
)
