package com.mvvm.home.widget

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.databinding.DataBindingUtil
import com.ca.feature.home.R
import com.ca.feature.home.databinding.LayoutPwMapBinding
import com.component.ext.setBackgroundAlpha
import com.component.widget.BasePupWindow


/**
 * Created by yan_x
 * @date 2021/11/18/018 10:32
 * @description
 */
class MapWindows: BasePupWindow {

    constructor(context: Activity):super(context){
        initView()
    }

    var onClick:(Int)->Unit={}

    private fun initView(){
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        // 设置SelectPicPopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.isFocusable = true
        this.isOutsideTouchable = false
        // 刷新状态
        this.update()
        // 实例化一个ColorDrawable颜色为半透明
        val dw = ColorDrawable(0x000000)
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw)
        this.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        val itemBinging: LayoutPwMapBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_pw_map,
            null,
            false
        )
        contentView = itemBinging.root
        itemBinging.tvBaidu.setOnClickListener {
            onClick.invoke(1)
            dismiss()
        }
        itemBinging.tvGaoDe.setOnClickListener {
            onClick.invoke(2)
            dismiss()
        }
        itemBinging.tvTent.setOnClickListener {
            onClick.invoke(3)
            dismiss()
        }
    }
    fun showView(view: View) {
        setBackgroundAlpha(0.5f, context!!)
        showAtLocation(view, Gravity.BOTTOM, 0, 0)
    }

    override fun dismiss() {
        super.dismiss()
        setBackgroundAlpha(1f, context!!)
    }
}