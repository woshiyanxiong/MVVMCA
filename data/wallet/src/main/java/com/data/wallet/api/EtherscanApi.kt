package com.data.wallet.api

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Url

interface NodeRealApi {
    
    @POST
    suspend fun getTransactions(
        @Url url: String,
        @Body request: NodeRealRequest
    ): NodeRealResponse?
}

data class NodeRealRequest(
    val jsonrpc: String = "2.0",
    val method: String = "nr_getTransactionByAddress",
    val params: List<TransactionParams>,
    val id: Int = 1
)

data class TransactionParams(
    val category: List<String> = listOf("external", "20"),
    val addressType: String = "",
    val address: String,
    val order: String = "desc",
    val excludeZeroValue: Boolean = false,
    val maxCount: String,
    val pageKey: String = ""
)

data class NodeRealResponse(
    val jsonrpc: String?,
    val id: Int?,
    val result: TransactionResult?
)

data class TransactionResult(
    val transfers: List<NodeRealTransaction>,
    val pageKey: String?
)

data class NodeRealTransaction(
    val hash: String,
    val from: String,
    val to: String,
    val value: String,
    val gasUsed: String,
    val gasPrice: String,
    val blockNum: String,
    val blockTimeStamp: String
)