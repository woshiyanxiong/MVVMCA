package com.data.wallet.model

/**
 * 区块链交易数据模型
 * 
 * @property hash 交易哈希值，唯一标识一笔交易
 * @property from 发送方地址
 * @property to 接收方地址
 * @property value 交易金额，以 Wei 为单位
 * @property gasUsed 实际消耗的 Gas 数量
 * @property gasPrice Gas 价格，以 Wei 为单位
 * @property blockNumber 交易所在的区块号
 * @property timestamp 交易时间戳（Unix 时间戳）
 * @property status 交易状态：success（成功）、failed（失败）、pending（待确认）
 */
data class TransactionModel(
    val hash: String,
    val from: String,
    val to: String,
    val value: String, // Wei 单位
    val gasUsed: String,
    val gasPrice: String,
    val blockNumber: String,
    val timestamp: Long,
    val status: String = "success" // success, failed, pending
) {
    /**
     * 判断是否为接收交易
     * 通过比较发送方和接收方地址是否相同来判断
     */
    val isReceive: Boolean
        get() = to.lowercase() == from.lowercase()
    
    /**
     * 交易类型
     * @return "receive" 表示接收交易，"send" 表示发送交易
     */    
    val type: String
        get() = if (isReceive) "receive" else "send"
}