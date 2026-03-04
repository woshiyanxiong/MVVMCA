package com.data.wallet.repo.impl

import com.data.wallet.api.AlchemyApi
import com.data.wallet.entity.AlchemyTokenBalanceRequest
import com.data.wallet.entity.AlchemyTokenMetadataRequest
import com.data.wallet.entity.TokenBalanceEntity
import com.data.wallet.repo.IAlchemyRepository
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

/**
 * Alchemy API 仓库实现
 * 负责通过 Alchemy 获取代币余额和元数据
 */
internal class AlchemyRepository @Inject constructor(
    private val alchemyApi: AlchemyApi
) : IAlchemyRepository {

    companion object {
        private const val ALCHEMY_URL = "https://eth-mainnet.g.alchemy.com/v2/nUELTIlKKB-hJR3k6X3FN"
    }

    override fun getTokenBalances(address: String): Flow<List<TokenBalanceEntity>> = flow {
        val request = AlchemyTokenBalanceRequest(
            params = listOf(address, "erc20")
        )
        val response = alchemyApi.getTokenBalances(ALCHEMY_URL, request)
        val tokenBalances = response?.result?.tokenBalances
        if (tokenBalances.isNullOrEmpty()) {
            emit(emptyList())
            return@flow
        }

        // 过滤掉余额为0的代币
        val nonZeroTokens = tokenBalances.filter { token ->
            val balance = token.tokenBalance ?: "0x0"
            balance != "0x0" && balance != "0x" && balance != "0"
        }

        // 查询每个代币的元数据
        val result = nonZeroTokens.mapNotNull { token ->
            try {
                val metaRequest = AlchemyTokenMetadataRequest(
                    params = listOf(token.contractAddress)
                )
                val metaResponse = alchemyApi.getTokenMetadata(ALCHEMY_URL, metaRequest)
                val meta = metaResponse?.result ?: return@mapNotNull null
                val decimals = meta.decimals ?: 18
                val rawBalance = BigInteger(token.tokenBalance!!.removePrefix("0x"), 16)
                val divisor = BigDecimal.TEN.pow(decimals)
                val balance = BigDecimal(rawBalance).divide(divisor, decimals, java.math.RoundingMode.DOWN)

                TokenBalanceEntity(
                    contractAddress = token.contractAddress,
                    name = meta.name ?: "Unknown",
                    symbol = meta.symbol ?: "???",
                    decimals = decimals,
                    balance = balance.stripTrailingZeros().toPlainString(),
                    logo = meta.logo
                )
            } catch (e: Exception) {
                LogUtils.e("AlchemyRepository", "获取代币元数据失败: ${e.message}")
                null
            }
        }
        emit(result)
    }.catch {
        LogUtils.e("AlchemyRepository", "获取代币余额失败: ${it.message}")
        emit(emptyList())
    }.flowOn(Dispatchers.IO)
}
