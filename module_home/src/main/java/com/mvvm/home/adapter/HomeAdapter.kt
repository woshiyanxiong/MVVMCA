package com.mvvm.home.adapter

import androidx.databinding.ObservableList
import com.common.base.BaseDataBingViewHolder
import com.common.base.BaseRecyclerViewAdapter
import com.mvvm.home.bean.HomeResponse
import com.mvvm.module_home.databinding.ItemHomeBinding

/**
 * Created by yan_x
 * @date 2021/11/15/015 16:56
 * @description
 */
class HomeAdapter(
    itemData: ObservableList<HomeResponse>,
    layoutId: Int,
    brId: Int
) : BaseRecyclerViewAdapter<HomeResponse, ItemHomeBinding>(itemData, layoutId, brId) {
    override fun bindViewHolder(
        viewHolder: BaseDataBingViewHolder<ItemHomeBinding>,
        position: Int,
        t: HomeResponse
    ) {
        super.bindViewHolder(viewHolder, position, t)
    }
}