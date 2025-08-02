package com.mvvm.home.adapter

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.ca.home.entity.DataX
import com.ca.router_compiler.CARouterApi
import com.component.base.BaseDataBingViewHolder
import com.component.base.BaseRecyclerViewAdapter
import com.mvvm.home.databinding.ItemHomeBinding
import com.mvvm.home.ui.DetailsActivity


/**
 * Created by yan_x
 * @date 2021/11/15/015 16:56
 * @description
 */
class HomeAdapter(
    layoutId: Int,
    brId: Int
) : BaseRecyclerViewAdapter<DataX, ItemHomeBinding>(layoutId, brId) {
    override fun bindViewHolder(
        viewHolder: BaseDataBingViewHolder<ItemHomeBinding>,
        position: Int,
        t: DataX?
    ) {
        super.bindViewHolder(viewHolder, position, t)
        viewHolder.binding.apply {
            tvName.text=t?.chapterName
            tvTitle.text=t?.title
            tvTime.text=t?.superChapterName
            root.setOnClickListener {
                CARouterApi.getInstance().navigation("/compose/main")
            }
        }
    }
}