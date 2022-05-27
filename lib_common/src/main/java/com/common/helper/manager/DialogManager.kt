package com.common.helper.manager


import android.util.Log
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator

/**
 * Created by yan_x
 * @date 2022/5/20/020 14:25
 * 弹窗管理器！
 * 我感觉这个的存在主要是为了防止重叠的情况
 * 外部需要一次性对多个弹框进行处理的情况！还是得外部处理 在什么时候开始执行弹框显示，内部只处理弹出
 */
class DialogManager private constructor() {

    companion object {
        /**
         * 对外提供一个对象管理器，主要用于当前页面的弹框管理
         * 这里注意！必须在页面销毁的时候清除队列的数据,不然会有存在内存泄漏的风险
         * @return YuPaoDialogManager
         */
        fun newDialogManager(): DialogManager {
            return DialogManager()
        }

        /**
         * 对外提供一个单列
         */
        val instance: DialogManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DialogManager()
        }
    }

    /**
     * 优先级队列 非线程安全
     * 不太可能同时存在12个弹框
     */
    private val queue =
        PriorityQueue(12, Comparator<IDialog> { o1, o2 -> o2.getPriority() - o1.getPriority() })

    /**
     * 当前队列栈顶的dialog
     */
    private var mDialogBase: IDialog? = null

    private var isRunning = false

    private var count = Int.MAX_VALUE - 10

    private var delayTime = 1000L


    /**
     * 添加弹框到队列
     * @param dialog IDialog? 弹框本身
     * @param isExecute Boolean 是否需要立即执行显示 默认不显示
     */
    @MainThread
    @Synchronized
    fun pushToQueue(dialog: IDialog?, isExecute: Boolean = false) {
        if (dialog == null) return
        this.add(dialog, isExecute)
    }

    private fun add(dialog: IDialog, isExecute: Boolean = false) {
        Log.e("添加的数据=", "$dialog")
        if (isEquallyPriority(dialog)) {
            val isFinish = doEquallyPriority(dialog)
            if (isFinish) return
        } else {
            queue.add(dialog)
        }
        dialog.setOnDismissListener {
            this.nextTask(dialog)
        }
        //为什么这里会有个优先级判断呢 因为避免同时add两个一样的dialog并且显示 而崩溃的问题，因为这种情况获取到的dialog.isShow是为FALSE的！当然同一个弹框不同优先级，同时添加并且显示这里必然gg
        if (isExecute && mDialogBase?.getPriority() != dialog.getPriority()) {
            startTask()
        }
    }

    /**
     * 外部调用开始队列执行
     */
    @MainThread
    @Synchronized
    fun execute() {
        startTask()
    }

    @MainThread
    fun clear() {
        queue.clear()
        if (mDialogBase?.isShowing() == true) {
            mDialogBase?.iDialogDismiss()
        }
        mDialogBase = null
    }

    /**
     * 当外部多次调用[execute]或者内部多次调用[nextTask]时需要判断当前是否可以显示
     * @return Boolean
     */
    @Synchronized
    private fun canNotShow(): Boolean {
        return queue.isEmpty() || mDialogBase != null
    }

    /**
     * 相同优先级的处理方式
     * @return Boolean 如果是拒绝入队的话 直接需要结束添加流程，如果是替换，判断当前弹框是否是同优先级并且是显示了，是则结束流程
     * true 结束添加流程 false继续添加流程
     *
     */
    private fun doEquallyPriority(dialog: IDialog?): Boolean {
        if (dialog?.getEquallyPriority() is EquallyPriorityType.Exclude) {
            return true
        }
        if (dialog?.getEquallyPriority() is EquallyPriorityType.Replace) {
            if (mDialogBase?.getPriority() == dialog.getPriority() && isShow()) {
                return true
            }
            removeIf {
                it.getPriority() == dialog.getPriority()
            }
            queue.add(dialog)
            return false
        }
        if (dialog?.getEquallyPriority() is EquallyPriorityType.ReplaceShowing) {
            if (mDialogBase == null) {
                removeIf {
                    it.getPriority() == dialog.getPriority()
                }
                queue.add(dialog)
                return false
            }
            if (mDialogBase?.getPriority() == dialog.getPriority()) {
                queue.add(dialog)
                mDialogBase?.iDialogDismiss()
                return true
            }
        }
        queue.add(dialog)
        return false
    }

    /**
     * 开始任务
     *
     */
    private fun startTask() {
        if (queue.isEmpty()) {
            mDialogBase = null
            return
        }
        if (canNotShow()) {
            return
        }
        try {
            mDialogBase = queue.element()
            if (mDialogBase?.isShowing() != true) {
                mDialogBase?.show()
                Log.e("show=", "${mDialogBase}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("弹框管理器发生异常", "$mDialogBase===" + e.message.toString())
            nextTask(null)
        }
    }

    /**
     * 开始下一个任务
     * @param dialog 當前頁面的弹框
     */
    private fun nextTask(dialog: IDialog?) {
        Log.e("nextTask", "nextTask")
        removeTopTask()
        dialogDismissMapData(dialog)
        startTask()
    }

    private fun isShow(): Boolean {
        return mDialogBase != null
    }

    /**
     * 后台检测器
     */
    private fun check() {
        if (isRunning) {
            return
        }
        isRunning = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            repeat(count + 2) {
                if (it == 0) return@repeat
                Log.e("计时器", "计时器")
                delay(delayTime)
                if (mDialogBase == null && !queue.isEmpty()) {
                    delay(1000)
                    if (mDialogBase == null && !queue.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            startTask()
                        }
                    }
                }
            }

        }
    }

    /**
     * 当前弹框消失的时候，需要根据互斥属性 处理一些数据
     */
    private fun dialogDismissMapData(dialog: IDialog?) {
        if (dialog?.isMutuallyExclusive() == true) {
            removeIf {
                it.isMutuallyExclusive()
            }
        }
    }

    /**
     * 是否有同一优先级的弹框
     * @param dialog IDialog? 传入的弹框
     * @return Boolean
     */
    private fun isEquallyPriority(dialog: IDialog?): Boolean {
        if (dialog == null) {
            return false
        }
        queue.forEach {
            if (it.getPriority() == dialog.getPriority()) {
                return true
            }
        }
        return false
    }

    /**
     * 删除某个数据
     * @param action Function1<IDialog, Boolean> 删除条件
     * @return Boolean 删除是否成功
     */
    private fun removeIf(action: (IDialog) -> Boolean): Boolean {
        var removed = false
        val each: MutableIterator<IDialog> = queue.iterator()
        while (each.hasNext()) {
            val dialog = each.next()
            if (action.invoke(dialog)) {
                each.remove()
                Log.e("YuPaoDialogManager", "删除数据=$dialog")
                removed = true
            }
        }
        return removed
    }

    /**
     * 移除队列的头,获取最新队列头
     */
    private fun removeTopTask() {
        queue.poll() //出栈
        mDialogBase = null
    }
}