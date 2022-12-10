package com.mvvm.home.ui


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.component.ext.loadMap
import com.component.ext.mapSuccess
import com.component.result.data
import com.component.uiStatus.IStatusView
import com.mvvm.home.bean.DataX
import com.mvvm.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/11/011 10:36
 * @description
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    val statusView: IStatusView
) : ViewModel() {

    private val _homeList = MutableLiveData<List<DataX>>()
    val homeList:LiveData<List<DataX>> = _homeList

    fun getHomeInfoList() {
        viewModelScope.launch {
            repository.getHomeInfoList(0).loadMap {
                statusView.addResource(it)
            }.mapSuccess {
                it.data?.let { it1 -> _homeList.value=it1.datas }
            }
        }
    }
}