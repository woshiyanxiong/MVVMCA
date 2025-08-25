package com.data.wallet.storage

import android.content.Context
import com.mvvm.storage.DataStorePre
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 钱包本地存储
 */
class WalletStore @Inject constructor(
    @ApplicationContext val context: Context
) {
    companion object {
        val walletStore = DataStorePre(WalletStore::class.java.name)
    }

    // 钱包列表存储键
    private val walletListKey = "walletListKey"
    // 当前选中钱包地址
    private val currentWalletAddressKey = "currentWalletAddressKey"
    // 钱包名称前缀
    private val walletNamePrefix = "walletName_"
    // 钱包文件名前缀
    private val walletFilePrefix = "walletFile_"

    /**
     * 保存钱包地址列表
     */
    suspend fun saveWalletList(addresses: List<String>) {
        val addressesString = addresses.joinToString(",")
        walletStore.saveData(context, walletListKey, addressesString)
    }

    /**
     * 获取钱包地址列表
     */
    fun getWalletList(): Flow<List<String>> {
        return walletStore.getData<String>(context, walletListKey, "").map { addressesString ->
            if (addressesString.isNullOrEmpty()) {
                emptyList()
            } else {
                addressesString.split(",").filter { it.isNotEmpty() }
            }
        }
    }

    /**
     * 添加新钱包地址
     */
    suspend fun addWalletAddress(address: String) {
        val currentList = getWalletList().first()
        val newList = currentList.toMutableList()
        if (!newList.contains(address)) {
            newList.add(address)
            saveWalletList(newList)
        }
    }

    /**
     * 保存钱包信息
     */
    suspend fun saveWalletInfo(address: String, name: String, fileName: String) {
        walletStore.saveData(context, walletNamePrefix + address, name)
        walletStore.saveData(context, walletFilePrefix + address, fileName)
        addWalletAddress(address)
    }

    /**
     * 获取钱包名称
     */
    fun getWalletName(address: String): Flow<String?> {
        return walletStore.getData<String>(context, walletNamePrefix + address, "")
    }

    /**
     * 获取钱包文件名
     */
    fun getWalletFileName(address: String): Flow<String?> {
        return walletStore.getData<String>(context, walletFilePrefix + address, "")
    }

    /**
     * 保存当前选中的钱包地址
     */
    suspend fun saveCurrentWalletAddress(address: String) {
        walletStore.saveData(context, currentWalletAddressKey, address)
    }

    /**
     * 获取当前选中的钱包地址
     */
    fun getCurrentWalletAddress(): Flow<String?> {
        return walletStore.getData<String>(context, currentWalletAddressKey, "")
    }

    /**
     * 删除钱包
     */
    suspend fun removeWallet(address: String) {
        val currentList = getWalletList().first()
        val newList = currentList.toMutableList()
        newList.remove(address)
        saveWalletList(newList)
        
        // 清除相关数据
        walletStore.saveData(context, walletNamePrefix + address, "")
        walletStore.saveData(context, walletFilePrefix + address, "")
    }
}