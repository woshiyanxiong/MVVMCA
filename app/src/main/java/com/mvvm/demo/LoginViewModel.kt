package com.mvvm.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.LoginBeanData
import com.component.ext.loadMap
import com.component.ext.mapSuccess
import com.component.result.data
import com.component.uiStatus.IStatusView
import com.mvvm.storage.LoginInfoUserCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 11:38
 * @description
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val loginInfoUserCase: LoginInfoUserCase,
    val statusView:IStatusView
) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess
        get() = _loginSuccess

    private val _userInfo = MutableStateFlow<LoginBeanData?>(null)
    private val userInfo = _userInfo.map {
        loginInfoUserCase(it?.username)
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    fun login(name: String, pwd: String) {
        viewModelScope.launch {
            repository.login(name, pwd).loadMap(statusView).mapSuccess {
                _userInfo.value = it.data
                _loginSuccess.value = true
            }
        }

    }
}