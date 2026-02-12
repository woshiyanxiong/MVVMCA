package com.data.wallet.storage

import android.content.Context
import com.data.wallet.model.NetworkInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mvvm.storage.DataStorePre
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val networkStore = DataStorePre(NetworkStore::class.java.name)
    }
    
    private val networksKey = "networks_list"
    private val currentNetworkKey = "current_network_id"
    private val defaultNetworkId = "ethereum-mainnet"
    private val gson = Gson()
    
    /**
     * 保存网络列表
     */
    suspend fun saveNetworks(networks: List<NetworkInfo>) {
        val networksJson = gson.toJson(networks)
        networkStore.saveData(context, networksKey, networksJson)
    }
    
    /**
     * 获取网络列表
     */
    fun getNetworks(): Flow<List<NetworkInfo>> {
        return networkStore.getData<String>(context, networksKey, "").map { json ->
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<NetworkInfo>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * 保存当前选中的网络ID
     */
    suspend fun saveCurrentNetworkId(networkId: String) {
        networkStore.saveData(context, currentNetworkKey, networkId)
    }
    
    /**
     * 获取当前选中的网络ID
     */
    fun getCurrentNetworkId(): Flow<String> {
        return networkStore.getData(context, currentNetworkKey, defaultNetworkId).map { it ?: defaultNetworkId }
    }
}