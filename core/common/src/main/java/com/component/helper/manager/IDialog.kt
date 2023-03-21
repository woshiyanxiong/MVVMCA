package com.component.helper.manager

/**
 * Created by yan_x
 * @date 2022/5/20/020 14:26
 * @description
 */
interface IDialog {
    /**
     * 是否是弹出
     * @return Boolean
     */
    fun isShowing(): Boolean

    /**
     * 弹出方法
     */
    fun show()

    /**
     * 设置优先级
     * @return Int
     */
    fun getPriority(): Int  //优先级

    /**
     * 弹框取消操作时需要实现该方法
     * @param listener Function0<Unit>
     */
    fun setOnDismissListener(listener: () -> Unit)

    /**
     * 是否是互斥的
     * 如果该属性为true 那么当弹框消失后，会清除剩下队列里面所有 互斥的弹框
     * @return Boolean
     */
    fun isMutuallyExclusive(): Boolean

    /**
     * 当出现优先级一样的时候处理方式,默认为拒绝入队
     */
    fun getEquallyPriority(): EquallyPriorityType


    /**
     * 取消 管理器主动调用关闭
     */
    fun iDialogDismiss()


}

/**
 * 存在相同优先级时的处理方式
 */
sealed class EquallyPriorityType {
    /**
     * 替换(前提是当前显示的弹框不是同一优先级，如果当前显示也要替换的话！使用下面那个)
     */
    object Replace : EquallyPriorityType()

    /**
     * 如果当前显示的弹框是同优先级就干掉它，然后篡位,使用这个必须实现[IDialog.iDialogDismiss()]方法
     */
    object ReplaceShowing : EquallyPriorityType()

    /**
     * 拒绝入队 保持优先级唯一！并且谁先入队谁就存在
     */
    object Exclude : EquallyPriorityType()

    /**
     * 共存 这里只是不同弹框，相同优先级
     * 同样的弹框最好存在一个
     * 非要同一个弹框自行处理好 可能存在的isAdd ，attache等错误，管理器不处理错误
     */
    object Coexist : EquallyPriorityType()
}

