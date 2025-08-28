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
    val isReceive:Boolean,
    val status: String = "success" // success, failed, pending
) {
}