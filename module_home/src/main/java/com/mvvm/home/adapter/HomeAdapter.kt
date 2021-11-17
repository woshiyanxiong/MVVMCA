package com.mvvm.home.adapter

import androidx.databinding.ObservableList
import com.common.base.BaseDataBingViewHolder
import com.common.base.BaseRecyclerViewAdapter
import com.mvvm.home.bean.DataX
import com.mvvm.module_home.databinding.ItemHomeBinding

/**
 * Created by yan_x
 * @date 2021/11/15/015 16:56
 * @description
 */
class HomeAdapter(
    itemData: ObservableList<DataX>,
    layoutId: Int,
    brId: Int
) : BaseRecyclerViewAdapter<DataX, ItemHomeBinding>(itemData, layoutId, brId) {
    override fun bindViewHolder(
        viewHolder: BaseDataBingViewHolder<ItemHomeBinding>,
        position: Int,
        t: DataX
    ) {
        super.bindViewHolder(viewHolder, position, t)
        viewHolder.binding.apply {
            tvName.text=t.chapterName
            tvTitle.text=t.title
            tvTime.text=t.superChapterName
        }
    }
}