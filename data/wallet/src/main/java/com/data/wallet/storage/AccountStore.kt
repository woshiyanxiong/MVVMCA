package com.data.wallet.storage

import android.content.Context
import com.data.wallet.model.AccountModel
import com.mvvm.logcat.LogUtils
import com.mvvm.storage.DataStorePre
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStore @Inject constructor(
    @ApplicationContext val context: Context
) {
    companion object {
        val accountStore = DataStorePre(AccountStore::class.java.name)
    }

    // 账户列表存储键
    private val accountListKey = "accountListKey"
    // 当前选中账户地址
    private val currentAccountKey = "currentAccountKey"
    // 账户名称前缀
    private val accountNamePrefix = "accountName_"
    // 账户私钥前缀
    private val accountPrivateKeyPrefix = "accountPrivateKey_"

    /**
     * 保存账户地址列表
     */
    suspend fun saveAccountList(addresses: List<String>) {
        val addressesString = addresses.joinToString(",")
        accountStore.saveData(context, accountListKey, addressesString)
    }

    /**
     * 获取账户地址列表
     */
    fun getAccountList(): Flow<List<String>> {
        return accountStore.getData<String>(context, accountListKey, "").map { addressesString ->
            if (addressesString.isNullOrEmpty()) {
                emptyList()
            } else {
                addressesString.split(",").filter { it.isNotEmpty() }
            }
        }
    }

    /**
     * 添加新账户地址
     */
    suspend fun addAccountAddress(address: String) {
        val currentList = getAccountList().first()
        val newList = currentList.toMutableList()
        if (!newList.contains(address)) {
            newList.add(address)
            saveAccountList(newList)
        }
    }

    /**
     * 保存账户信息
     */
    suspend fun saveAccount(account: AccountModel) {
        accountStore.saveData(context, accountNamePrefix + account.address, account.name)
        accountStore.saveData(context, accountPrivateKeyPrefix + account.address, account.privateKey)
        addAccountAddress(account.address)
    }

    /**
     * 获取账户名称
     */
    fun getAccountName(address: String): Flow<String?> {
        return accountStore.getData<String>(context, accountNamePrefix + address, "")
    }

    /**
     * 获取账户私钥
     */
    fun getAccountPrivateKey(address: String): Flow<String?> {
        return accountStore.getData<String>(context, accountPrivateKeyPrefix + address, "")
    }

    /**
     * 获取所有账户
     */
    fun getAllAccounts(): Flow<List<AccountModel>> {
        return getAccountList().map { addresses ->
            addresses.mapNotNull { address ->
                val name = getAccountName(address).first()
                val privateKey = getAccountPrivateKey(address).first()
                if (!name.isNullOrEmpty() && !privateKey.isNullOrEmpty()) {
                    AccountModel(address = address, name = name, privateKey = privateKey)
                } else {
                    null
                }
            }
        }
    }

    /**
     * 保存当前选中的账户地址
     */
    suspend fun setCurrentAccount(address: String) {
        accountStore.saveData(context, currentAccountKey, address)
    }

    /**
     * 获取当前选中的账户地址
     */
    fun getCurrentAccountAddress(): Flow<String?> {
        return accountStore.getData<String>(context, currentAccountKey, "")
    }

    /**
     * 获取当前账户
     */
    fun getCurrentAccount(): Flow<AccountModel?> {
        return getCurrentAccountAddress().map { address ->
            if (!address.isNullOrEmpty()) {
                val name = getAccountName(address).first()
                val privateKey = getAccountPrivateKey(address).first()
                if (!name.isNullOrEmpty() && !privateKey.isNullOrEmpty()) {
                    AccountModel(address = address, name = name, privateKey = privateKey)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * 删除账户
     */
    suspend fun deleteAccount(address: String) {
        val currentList = getAccountList().first()
        val newList = currentList.toMutableList()
        newList.remove(address)
        saveAccountList(newList)
        
        // 清除相关数据
        accountStore.saveData(context, accountNamePrefix + address, "")
        accountStore.saveData(context, accountPrivateKeyPrefix + address, "")
        
        // 如果删除的是当前账户，清除当前账户设置
        val currentAddress = getCurrentAccountAddress().first()
        if (currentAddress == address) {
            accountStore.saveData(context, currentAccountKey, "")
        }
    }

    /**
     * 更新账户名称
     */
    suspend fun updateAccountName(address: String, name: String) {
        accountStore.saveData(context, accountNamePrefix + address, name)
    }
    
    /**
     * 保存钱包密码（用于Web3j钱包加密）
     */
    suspend fun saveWalletPassword(password: String) {
        // 直接存储原始密码，用于Web3j钱包操作
        accountStore.saveData(context, "walletPassword", password)
    }
    
    /**
     * 获取钱包密码
     */
    fun getWalletPassword(): Flow<String?> {
        return accountStore.getData<String>(context, "walletPassword", "")
    }
    
    /**
     * 验证钱包密码
     */
    suspend fun verifyWalletPassword(inputPassword: String): Boolean {
        val storedPassword = getWalletPassword().first()
        LogUtils.e("本地密码",storedPassword)
        return storedPassword == inputPassword
    }
}