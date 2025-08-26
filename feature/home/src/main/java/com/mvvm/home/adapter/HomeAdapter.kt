package com.mvvm.home.adapter

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.ca.home.entity.DataX
import com.ca.router_compiler.CARouterApi
import com.component.base.BaseDataBingViewHolder
import com.component.base.BaseRecyclerViewAdapter
import com.mvvm.home.R
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
    private val image = listOf(R.drawable.post_1, R.drawable.post_2, R.drawable.post_3, R.drawable.post_4, R.drawable.post_5, R.drawable.post_6,)
    override fun bindViewHolder(
        viewHolder: BaseDataBingViewHolder<ItemHomeBinding>,
        position: Int,
        t: DataX?
    ) {
        super.bindViewHolder(viewHolder, position, t)
        viewHolder.binding.apply {
            tvName.text=t?.chapterName
            tvTitle.text=t?.title
            tvTime.text=t?.niceShareDate
            icon.setImageResource(image[(Math.random() * 6).toInt()])
            root.setOnClickListener {
                CARouterApi.getInstance().navigation("/compose/splash")
            }
        }
    }
}