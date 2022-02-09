package com.mvvm.demo

import androidx.lifecycle.MutableLiveData
import com.common.ext.launch
import com.common.viewmodel.BaseViewModel
import com.mvvm.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 11:38
 * @description
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: UserRepository) : BaseViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess
        get() = _loginSuccess

    fun login(name: String, pwd: String) {
        launch({
            repository.login(name, pwd)
        }, {
            _loginSuccess.value=true
        }, stateView)

    }
}