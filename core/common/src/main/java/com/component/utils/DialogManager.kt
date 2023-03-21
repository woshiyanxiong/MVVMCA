package com.component.utils


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.lang.Exception
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Created by yan_x
 * @time 2020/8/18
 * 多弹窗依次弹出显示管理,在非mainActivity中单独使用时 退出当前activity或fragment时
 * 必须清空队列，否则会存在内存泄漏(最好用对象)
 * 使用优先级队列非线程安全，一定要在主线程使用
 */
class DialogManager {
    //    //弹窗队列(线程安全)
//    private val queue = ConcurrentLinkedQueue<OrderDialog>()
    private val queue =
        PriorityQueue(11, Comparator<OrderDialog> { o1, o2 -> o2.getPriority() - o1.getPriority() })
    private var mDialogBase: OrderDialog? = null

    private var isBlocking = false

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            if (msg.arg1==1001){
                startNextIf()
            }
        }
    }

    private var isCanShow: Boolean = true
        set(value) {
            field = value
            field.yes {
                if (mDialogBase?.isShowing() == false)
                    startNextIf()
            }.no {
            }

        }

    /**
     * 初始为0,pushToQueue 基数必然为1,所以小于2并且可以展示即可
     *
     * @return
     */
    @Synchronized
    private fun canShow(): Boolean {
        return queue.size < 2 && isCanShow /* && !isLock*/
    }

    /**
     * 每次弹窗调用PushQueue方法
     *
     * @param dialogBase
     */

    fun pushToQueue(dialogBase: OrderDialog?, isShow: Boolean = true) {
        //添加到队列中
        logE("pushToQueue", "添加数据前队列数据${queue}")
        logE("pushToQueue", "添加数据${dialogBase}")
        if (dialogBase == null) return
        if (queue.contains(dialogBase).not() && !getContainsDialog(dialogBase)) {
            dialogBase.setOnDismissListener {
                nextTask()
            }
            queue.add(dialogBase)
            interiorSort()
            //只有当前队列数量为1时才能进行下一步操作
            if (isShow) {
                logE("pushToQueue", "开始排列")
                queueSort()
            }
        }
    }

    private fun getContainsDialog(dialogBase: OrderDialog): Boolean {
        queue.forEach {
            if (it.getPriority() == dialogBase.getPriority()) {
                return true
            }
        }
        return false
    }

    /**
     * 阻塞
     * @param isBlocking Boolean
     */
    fun setBlocking(isBlocking: Boolean) {
        this.isBlocking=isBlocking
    }

    /**
     * 开启队列循环 这玩意应该用不到
     */
    private fun startQueueLooper() {
        Thread {
            while (true) {
                try {
                    Thread.sleep(1000)
                    if (isBlocking) {
                        break
                    }else{
                        val msg=handler.obtainMessage()
                        msg.arg1=1001
                        handler.dispatchMessage(msg)
                    }
                }catch (e:Exception){}
            }
        }.start()
    }

    /**
     * 显示下一个弹窗任务
     */
    private fun startNextIf() {
        if (queue.isEmpty() || isCanShow.not()) {
            return
        }
        if (mDialogBase != null && mDialogBase?.isShowing() == true) {
            return
        }
        oderDialog()
        mDialogBase = queue.element()
        if (mDialogBase != null && mDialogBase!!.isShowing().not()) {
            mDialogBase?.show()
        }
    }

    private fun oderDialog() {

        //可在此处对弹窗进行排序
        //因为是多个接口组合 安装顺序添加进来的 所以不需要再排序
    }

    /**
     * 提供外部下一个任务的方法,在弹窗消失时候调用
     */
    private fun nextTask() {
        removeTopTask()
        startNextIf()
    }

    /**
     * 移除队列的头,获取最新队列头
     */
    private fun removeTopTask() {
        queue.poll() //出栈
    }

    fun removeAll() {
        queue.removeAll(queue.requireNoNulls())
    }

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = DialogManager()
    }

    fun queueSort() {
        val comparator = Comparator<OrderDialog> { o1, o2 -> o2.getPriority() - o1.getPriority() }
        logE("队列内部排序前", queue.toList().toString())
        val list: ArrayList<OrderDialog> = ArrayList(queue.toList())
        Collections.sort(list, comparator)
        logE("队列内部排序后——list", list.toString())
        logE("队列内部排序后", queue.toList().toString())
        startNextIf()
    }

    private fun interiorSort() {
        logE("队列interiorSort后", queue.toList().toString())
    }

    /**
     * 删除存在的优先级
     */
    fun removeSame(priority: Int) {
        var orderDialog: OrderDialog? = null
        queue.forEach {
            if (it.getPriority() == priority) {
                orderDialog = it
            }
        }
        orderDialog?.let { queue.remove(orderDialog) }
    }

    private fun logE(tag: String, msg: String) {
        Log.e(tag, msg)
    }


}

interface OrderDialog {
    fun isShowing(): Boolean

    fun show()

    fun getPriority(): Int  //优先级

    fun setOnDismissListener(listener: () -> Unit)
}

@OptIn(ExperimentalContracts::class)
private inline fun Boolean?.yes(block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this == true) block()
    return this
}

@OptIn(ExperimentalContracts::class)
private inline fun Boolean?.no(block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this != true) block()
    return this
}
