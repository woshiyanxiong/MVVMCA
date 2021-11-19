package com.mvvm.mine.ui

import com.common.viewmodel.BaseViewModel
import com.mvvm.mine.repository.MineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2020/11/19/019 16:24
 * @description
 */
@HiltViewModel
class MineViewModel @Inject constructor(private val repository: MineRepository) : BaseViewModel() {
    fun getUserInfo() {
        async({
            repository.getUserInfo()
        }, {

        })
    }
}