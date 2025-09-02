package com.data.wallet.repo.impl

import com.data.wallet.model.AccountModel
import com.data.wallet.repo.IAccountRepository
import com.data.wallet.storage.AccountStore
import com.mvvm.logcat.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject

internal class AccountRepository @Inject constructor(
    private val accountStore: AccountStore
) : IAccountRepository {
    
    private val nodeUrl = "https://eth-mainnet.nodereal.io/v1/54f700c58aeb4d2fb2620b817759894e"
    private val web3: Web3j = Web3j.build(HttpService(nodeUrl))
    
    override fun getAccountList(): Flow<List<AccountModel>> {
        return accountStore.getAllAccounts().catch {
            LogUtils.e("获取账户列表失败: ${it.message}")
            emit(emptyList())
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getCurrentAccount(): Flow<AccountModel?> {
        return accountStore.getCurrentAccount().catch {
            LogUtils.e("获取当前账户失败: ${it.message}")
            emit(null)
        }.flowOn(Dispatchers.IO)
    }
    
    override fun createAccount(name: String): Flow<AccountModel?>  {
       return flow<AccountModel?> {
            val credentials = Credentials.create(Keys.createEcKeyPair())
            val account = AccountModel(
                address = credentials.address,
                name = name,
                privateKey = credentials.ecKeyPair.privateKey.toString(16)
            )

            accountStore.saveAccount(account)
            emit(account)
        }.catch {
            LogUtils.e("创建账户失败: ${it.message}")
            emit(null)
        }.flowOn(Dispatchers.IO)
    }
    
    override fun importAccount(privateKey: String, name: String): Flow<AccountModel?> = flow<AccountModel?> {
        val credentials = Credentials.create(privateKey)
        val account = AccountModel(
            address = credentials.address,
            name = name,
            privateKey = privateKey
        )
        
        accountStore.saveAccount(account)
        emit(account)
    }.catch {
        LogUtils.e("导入账户失败: ${it.message}")
        emit(null)
    }.flowOn(Dispatchers.IO)
    
    override fun switchAccount(address: String): Flow<Boolean> = flow {
        accountStore.setCurrentAccount(address)
        emit(true)
    }.catch {
        LogUtils.e("切换账户失败: ${it.message}")
        emit(false)
    }.flowOn(Dispatchers.IO)
    
    override fun deleteAccount(address: String): Flow<Boolean> = flow {
        accountStore.deleteAccount(address)
        emit(true)
    }.catch {
        LogUtils.e("删除账户失败: ${it.message}")
        emit(false)
    }.flowOn(Dispatchers.IO)

    
    override fun updateAccountName(address: String, name: String): Flow<Boolean> = flow {
        accountStore.updateAccountName(address, name)
        emit(true)
    }.catch {
        LogUtils.e("更新账户名称失败: ${it.message}")
        emit(false)
    }.flowOn(Dispatchers.IO)
    
    override fun saveWalletPassword(password: String): Flow<Boolean> = flow {
        accountStore.saveWalletPassword(password)
        emit(true)
    }.catch {
        LogUtils.e("保存钱包密码失败: ${it.message}")
        emit(false)
    }.flowOn(Dispatchers.IO)

    override fun getWalletPassword(): Flow<String?> {
        return accountStore.getWalletPassword()
    }
    
    override fun verifyWalletPassword(password: String): Flow<Boolean> = flow {
        val isValid = accountStore.verifyWalletPassword(password)
        emit(isValid)
    }.catch {
        LogUtils.e("验证密码失败: ${it.message}")
        emit(false)
    }.flowOn(Dispatchers.IO)
}