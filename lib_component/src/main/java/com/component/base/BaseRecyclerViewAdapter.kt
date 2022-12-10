package com.component.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<T, Vb : ViewDataBinding>(
    var layoutId: Int,
    var dataId: Int? = null
) : RecyclerView.Adapter<BaseDataBingViewHolder<Vb>>() {
    private var data = mutableListOf<T>()
    private lateinit var bing: Vb
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): BaseDataBingViewHolder<Vb> {
        bing = DataBindingUtil.inflate<Vb>(
            LayoutInflater.from(viewGroup.context),
            layoutId,
            viewGroup,
            false
        )
        return BaseDataBingViewHolder(bing)
    }


    override fun onBindViewHolder(viewHolder: BaseDataBingViewHolder<Vb>, i: Int) {
        dataId?.let { viewHolder.binding.setVariable(it, data[i]) }
        bindViewHolder(viewHolder, i, data[i])

    }

    protected open fun bindViewHolder(
        @NonNull viewHolder: BaseDataBingViewHolder<Vb>,
        position: Int,
        t: T?
    ) {
    }

    override fun getItemCount(): Int {
        return data.size
    }


    fun addNewData(newItemData: List<T>, isClear: Boolean = false) {
        if (isClear) {
            data.clear()
        }
        data.addAll(newItemData)
        notifyDataSetChanged()
    }

    fun addData(newItemData: T) {
        data.add(newItemData)
        notifyDataSetChanged()
    }
}