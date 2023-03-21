package com.mvvm.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2022/12/10/010 14:13
 * @description
 */
class LoginStore @Inject constructor(
    @ApplicationContext val context: Context
) {
    companion object {
        val loginStore = DataStorePre(LoginStore::class.java.name)
    }

    private val loginTokenKey = "loginTokenKey"
    suspend fun saveLoginToken(data: String) {
        loginStore.saveData(context, loginTokenKey, data)
    }

    fun getLoginToken(): Flow<String?> {
        return loginStore.getData<String>(context, loginTokenKey, "")
    }
}