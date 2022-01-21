package com.mvvm.home.ui

import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.viewModelScope
import com.common.ext.launch
import com.common.result.data
import com.common.result.handle
import com.common.viewmodel.BaseViewModel
import com.google.gson.Gson
import com.mvvm.home.bean.DataX
import com.mvvm.home.repository.HomeRepository
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 10:36
 * @description
 */
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : BaseViewModel() {

    var homeData: ObservableList<DataX> = ObservableArrayList()

    fun getHomeInfoList() {
        launch(
            { repository.getHomeInfoList(0) }, {
                it.data?.let { it1 -> homeData.addAll(it1.datas) }
                Log.e("当前线程=", Thread.currentThread().name)
            },
            stateView
        )
    }
}