package com.mvvm.storage

import javax.inject.Inject

/**
 * Created by yan_x
 * 存信息
 * @date 2022/12/10/010 14:32
 * @description
 */
class LoginInfoUserCase @Inject constructor(private val loginStore: LoginStore) {


    suspend operator fun invoke(token: String?) {
        token?.let { loginStore.saveLoginToken(it) }
    }
}