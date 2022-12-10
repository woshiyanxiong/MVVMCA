package com.mvvm.mine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvvm.mine.repository.MineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2020/11/19/019 16:24
 * @description
 */
@HiltViewModel
class MineViewModel @Inject constructor(private val repository: MineRepository) : ViewModel() {
    fun getUserInfo() {
        viewModelScope.launch {
            repository.getUserInfo()
        }

    }
}