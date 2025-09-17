package com.data.wallet.repo

import com.data.wallet.model.NetworkConfigData
import com.data.wallet.model.NetworkInfo
import kotlinx.coroutines.flow.Flow

interface INetworkRepository {
    
    /**
     * 获取当前选中的网络
     */
    fun getCurrentNetwork(): Flow<NetworkInfo?>
    
    /**
     * 获取所有网络配置数据
     */
    fun getAllNetworkConfig(): Flow<NetworkConfigData?>
    
    /**
     * 切换网络
     */
    suspend fun switchNetwork(networkId: String): Boolean
    
    /**
     * 获取当前网络ID
     */
    fun getCurrentNetworkId(): Flow<String>

    suspend fun initNetWork()



}