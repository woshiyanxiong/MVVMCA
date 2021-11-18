package com.mvvm.mine.ui

import android.widget.Toast
import com.common.base.BaseFragment
import com.mvvm.home.HomePath
import com.mvvm.home.api.MapNavigation

import com.mvvm.mine.R
import com.mvvm.mine.databinding.FragmentMineBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/18/018 11:07
 * @description
 */
@AndroidEntryPoint
class MineFragment : BaseFragment<FragmentMineBinding>() {

    @Inject
    lateinit var mapWindows: MapNavigation

    override fun getLayout(): Int = R.layout.fragment_mine

    override fun initView() {
        HomePath.HOME_DETAILS
        mapWindows.createMap(requireActivity()) {
            Toast.makeText(requireContext(),"$it",Toast.LENGTH_SHORT).show()
        }
        binding.show.setOnClickListener {
            mapWindows.show(binding.root)
        }

    }

    override fun loadData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mapWindows.disses()
    }
}