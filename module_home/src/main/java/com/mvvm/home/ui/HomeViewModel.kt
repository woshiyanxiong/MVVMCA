package com.mvvm.home.ui

import android.util.Log
import com.common.viewmodel.BaseViewModel
import com.mvvm.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 10:36
 * @description
 */
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : BaseViewModel() {
    fun getHomeInfoList(){
        async({
            repository.getHomeInfoList(0)
        },{

        },{

        },{
            Log.e("当前线程",Thread.currentThread().name)
        })
    }
}