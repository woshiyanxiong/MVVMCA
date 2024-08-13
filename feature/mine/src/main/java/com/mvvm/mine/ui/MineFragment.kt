package com.mvvm.mine.ui

import android.widget.Toast
import androidx.fragment.app.viewModels
import com.component.base.BaseFragment
import com.component.helper.image.ImageLoadApp
import com.example.api.MapNavigation


import com.mvvm.mine.R
import com.mvvm.mine.databinding.FragmentMineBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2020/11/18/018 11:07
 * @description
 */
@AndroidEntryPoint
class MineFragment : BaseFragment<FragmentMineBinding>() {

//    @Inject
//    lateinit var mapWindows: MapNavigation

    override fun getLayout(): Int = R.layout.fragment_mine

    private val imageUrl="https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201906%2F27%2F20190627073618_hppyq.thumb.400_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1642234000&t=d55876f66955deeee1fdee8740c817c8"

    private val viewModel: MineViewModel by viewModels()

    override fun initView() {
//        mapWindows.createMap(requireActivity()) {
//            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
//        }
//        binding.show.setOnClickListener {
//            mapWindows.show(binding.root)
//        }
        ImageLoadApp.loadImage(binding.image,imageUrl)
        getLog()
    }

    override fun loadData() {
        viewModel.getUserInfo()
    }

    private fun getLog(){
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}