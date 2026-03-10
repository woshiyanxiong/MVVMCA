package com.data.wallet.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Wei / ETH 单位转换工具
 */
object WeiConverter {
    private val WEI_IN_ETH = BigDecimal("1000000000000000000")

    /** ETH 原生代币占位合约地址 */
    const val ETH_ADDRESS = "0x0000000000000000000000000000000000000000"

    /**
     * Wei 转 ETH（BigDecimal）
     */
    fun weiToEth(wei: BigInteger?): BigDecimal {
        return BigDecimal(wei ?: BigInteger.ZERO)
            .divide(WEI_IN_ETH, 18, RoundingMode.DOWN)
            .stripTrailingZeros()
    }

    /**
     * Wei 转 ETH 字符串
     */
    fun weiToEthString(wei: BigInteger?): String {
        return weiToEth(wei).toPlainString()
    }

    /**
     * Wei 字符串（十六进制或十进制）转 ETH 字符串
     */
    fun weiHexToEthString(weiHex: String): String {
        return try {
            val weiValue = if (weiHex.startsWith("0x", ignoreCase = true)) {
                BigInteger(weiHex.removePrefix("0x").removePrefix("0X"), 16)
            } else {
                BigInteger(weiHex)
            }
            weiToEthString(weiValue)
        } catch (e: Exception) {
            "0"
        }
    }
}
