package com.data.wallet.repo

import com.data.wallet.entity.TokenBalanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Alchemy API 仓库接口
 * 负责通过 Alchemy 获取代币余额和元数据
 */
interface IAlchemyRepository {
    
    /**
     * 获取指定地址的所有 ERC20 代币余额
     * @param address 钱包地址
     * @return 代币余额列表
     */
    fun getTokenBalances(address: String): Flow<List<TokenBalanceEntity>>
}
