package com.mvvm.home.ui

import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.common.viewmodel.BaseViewModel
import com.google.gson.Gson
import com.mvvm.home.bean.DataX
import com.mvvm.home.repository.HomeRepository
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
        async({
            repository.getHomeInfoList(0)
        }, {
            homeData.addAll(it.data.datas)
            LogUtils.e("获取到的数据=${Gson().toJson(homeData)}")
        }, showDialog = true)
    }
}