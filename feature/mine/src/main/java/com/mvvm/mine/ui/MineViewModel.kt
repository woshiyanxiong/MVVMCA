package com.mvvm.mine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camine.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2020/11/19/019 16:24
 * @description
 */
@HiltViewModel
class MineViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {
    fun getUserInfo() {
        viewModelScope.launch {
            repository.getUserInfo()
        }

    }
}