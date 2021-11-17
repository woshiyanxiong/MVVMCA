package com.mvvm.home.ui

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.common.base.BaseFragment
import com.mvvm.home.adapter.HomeAdapter
import com.mvvm.module_home.R
import com.mvvm.module_home.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by yan_x
 * @date 2021/11/5/005 17:28
 * @description
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private val adapter by lazy {
        HomeAdapter(viewModel.homeData, R.layout.item_home, 0)
    }

    override fun getLayout(): Int = R.layout.fragment_home

    override fun initView() {
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.adapter = adapter
    }

    override fun loadData() {
        viewModel.getHomeInfoList()
    }

}