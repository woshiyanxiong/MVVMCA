package com.component.helper.manager

/**
 * Created by yan_x
 * @date 2022/5/20/020 14:36
 * 抽一个默认接口
 * 主要实现一些并非每个弹框都需要实现的属性
 */
interface IDefaultDialog : IDialog {

    override fun isMutuallyExclusive(): Boolean {
        return false
    }

    override fun getEquallyPriority(): EquallyPriorityType {
        return EquallyPriorityType.Exclude
    }

}