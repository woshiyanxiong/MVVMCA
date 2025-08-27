package com.data.wallet.model

data class TransactionsRequest(
    val module: String = "account",
    val action: String = "txlist",
    val address: String,
    val startblock: Int = 0,
    val endblock: Int = 99999999,
    val page: Int = 1,
    val offset: Int = 10,
    val sort: String = "desc",
    val apikey: String?,
)
