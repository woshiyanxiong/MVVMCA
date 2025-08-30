package com.data.wallet.model

import java.math.BigInteger

/**
 * 账户模型
 * @param address 账户地址
 * @param name 账户名称
 * @param privateKey 私钥
 * @param balance 余额
 * @param isSelected 是否为当前选中账户
 */
data class AccountModel(
    val address: String,
    val name: String,
    val privateKey: String,
    val balance: BigInteger = BigInteger.ZERO,
    val isSelected: Boolean = false
)