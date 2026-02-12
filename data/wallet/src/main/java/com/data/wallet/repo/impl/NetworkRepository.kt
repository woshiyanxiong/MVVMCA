package com.data.wallet.repo.impl

import android.content.Context
import com.data.wallet.model.NetworkConfigData
import com.data.wallet.model.NetworkInfo
import com.data.wallet.repo.INetworkRepository
import com.data.wallet.storage.NetworkStore
import com.google.gson.Gson
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

internal class NetworkRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkStore: NetworkStore
) : INetworkRepository {
    
    private val gson = Gson()
    
    override fun getCurrentNetwork(): Flow<NetworkInfo?> {
        return getCurrentNetworkId().map { networkId ->
            networkStore.getNetworks().first().find { it.id == networkId }
        }.catch {
            LogUtils.e("获取当前网络失败: ${it.message}")
            emit(null)
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getAllNetworkConfig(): Flow<NetworkConfigData?> = flow {
        try {
            // 先从本地存储获取
            val localNetworks = networkStore.getNetworks().first()
            if (localNetworks.isNotEmpty()) {
                val config = NetworkConfigData(networks = localNetworks, tokens = emptyList())
                emit(config)
                return@flow
            }
            
            // 本地没有数据，从 assets 读取
            val jsonString = context.assets.open("networks.json").bufferedReader().use { it.readText() }
            val config = gson.fromJson(jsonString, NetworkConfigData::class.java)
            
            // 保存到本地
            config?.networks?.let { networkStore.saveNetworks(it) }
            
            emit(config)
        } catch (e: IOException) {
            LogUtils.e("读取网络配置文件失败: ${e.message}")
            emit(null)
        } catch (e: Exception) {
            LogUtils.e("解析网络配置失败: ${e.message}")
            emit(null)
        }
    }.catch {
        LogUtils.e("获取网络配置异常: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)
    
    override suspend fun switchNetwork(networkId: String): Boolean {
        return try {
            networkStore.saveCurrentNetworkId(networkId)
            LogUtils.d("切换网络成功:","$networkId")
            true
        } catch (e: Exception) {
            LogUtils.e("切换网络失败: ${e.message}")
            false
        }
    }
    
    override fun getCurrentNetworkId(): Flow<String> {
        return networkStore.getCurrentNetworkId()
    }

    override suspend fun initNetWork() {

    }
}