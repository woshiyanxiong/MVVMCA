package com.data.wallet.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.data.wallet.model.AccountModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.accountDataStore: DataStore<Preferences> by preferencesDataStore(name = "account_prefs")

@Singleton
class AccountStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val accountListKey = stringPreferencesKey("account_list")
    private val currentAccountKey = stringPreferencesKey("current_account")
    
    /**
     * 获取所有账户
     */
    fun getAllAccounts(): Flow<List<AccountModel>> {
        return context.accountDataStore.data.map { preferences ->
            val json = preferences[accountListKey] ?: "[]"
            val type = object : TypeToken<List<AccountModel>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    /**
     * 获取当前账户
     */
    fun getCurrentAccount(): Flow<AccountModel?> {
        return context.accountDataStore.data.map { preferences ->
            val address = preferences[currentAccountKey]
            if (address != null) {
                val accounts = getAllAccounts()
                // 这里需要同步获取，实际使用中可能需要优化
                null // 临时返回null，实际需要根据address查找
            } else {
                null
            }
        }
    }
    
    /**
     * 保存账户
     */
    suspend fun saveAccount(account: AccountModel): Flow<Unit> {
        context.accountDataStore.edit { preferences ->
            val json = preferences[accountListKey] ?: "[]"
            val type = object : TypeToken<List<AccountModel>>() {}.type
            val accounts = gson.fromJson<List<AccountModel>>(json, type)?.toMutableList() ?: mutableListOf()
            
            // 检查是否已存在
            val existingIndex = accounts.indexOfFirst { it.address == account.address }
            if (existingIndex >= 0) {
                accounts[existingIndex] = account
            } else {
                accounts.add(account)
            }
            
            preferences[accountListKey] = gson.toJson(accounts)
        }
        return kotlinx.coroutines.flow.flowOf(Unit)
    }
    
    /**
     * 设置当前账户
     */
    suspend fun setCurrentAccount(address: String): Flow<Unit> {
        context.accountDataStore.edit { preferences ->
            preferences[currentAccountKey] = address
        }
        return kotlinx.coroutines.flow.flowOf(Unit)
    }
    
    /**
     * 删除账户
     */
    suspend fun deleteAccount(address: String): Flow<Unit> {
        context.accountDataStore.edit { preferences ->
            val json = preferences[accountListKey] ?: "[]"
            val type = object : TypeToken<List<AccountModel>>() {}.type
            val accounts = gson.fromJson<List<AccountModel>>(json, type)?.toMutableList() ?: mutableListOf()
            
            accounts.removeAll { it.address == address }
            preferences[accountListKey] = gson.toJson(accounts)
            
            // 如果删除的是当前账户，清除当前账户设置
            if (preferences[currentAccountKey] == address) {
                preferences.remove(currentAccountKey)
            }
        }
        return kotlinx.coroutines.flow.flowOf(Unit)
    }
    
    /**
     * 更新账户名称
     */
    suspend fun updateAccountName(address: String, name: String): Flow<Unit> {
        context.accountDataStore.edit { preferences ->
            val json = preferences[accountListKey] ?: "[]"
            val type = object : TypeToken<List<AccountModel>>() {}.type
            val accounts = gson.fromJson<List<AccountModel>>(json, type)?.toMutableList() ?: mutableListOf()
            
            val index = accounts.indexOfFirst { it.address == address }
            if (index >= 0) {
                accounts[index] = accounts[index].copy(name = name)
                preferences[accountListKey] = gson.toJson(accounts)
            }
        }
        return kotlinx.coroutines.flow.flowOf(Unit)
    }
}