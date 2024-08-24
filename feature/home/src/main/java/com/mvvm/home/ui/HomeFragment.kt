package com.mvvm.home.ui

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.component.base.BaseFragment
import com.component.uiStatus.IUiLoadStatus
import com.mvvm.home.R
import com.mvvm.home.adapter.HomeAdapter
import com.mvvm.home.databinding.FragmentHomeBinding

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/5/005 17:28
 * @description
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var loadStatus: IUiLoadStatus

    private val adapter by lazy {
        HomeAdapter(R.layout.item_home, 0)
    }

    override fun getLayout(): Int = R.layout.fragment_home

    override fun initView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.adapter = adapter
        binding.recycleView.itemAnimator
        loadStatus.initUiStatus(this,viewModel.statusView)
        initObserve()
    }

    override fun loadData() {
        viewModel.getHomeInfoList()
    }

    private fun initObserve(){
        viewModel.homeList.observe(this){
            adapter.addNewData(it)
            Log.e("数据","${it.size}")
        }
    }
}