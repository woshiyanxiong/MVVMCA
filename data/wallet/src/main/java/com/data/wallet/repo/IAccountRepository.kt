package com.data.wallet.repo

import com.data.wallet.model.AccountModel
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

interface IAccountRepository {
    
    /**
     * 获取所有账户列表
     */
    fun getAccountList(): Flow<List<AccountModel>>
    
    /**
     * 获取当前选中的账户
     */
    fun getCurrentAccount(): Flow<AccountModel?>
    
    /**
     * 创建新账户
     */
    fun createAccount(name: String): Flow<AccountModel?>
    
    /**
     * 导入账户
     */
    fun importAccount(privateKey: String, name: String): Flow<AccountModel?>
    
    /**
     * 切换当前账户
     */
    fun switchAccount(address: String): Flow<Boolean>
    
    /**
     * 删除账户
     */
    fun deleteAccount(address: String): Flow<Boolean>

    
    /**
     * 更新账户名称
     */
    fun updateAccountName(address: String, name: String): Flow<Boolean>
}