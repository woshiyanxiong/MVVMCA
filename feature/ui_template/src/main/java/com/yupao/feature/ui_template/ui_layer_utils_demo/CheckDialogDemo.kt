package com.yupao.feature.ui_template.ui_layer_utils_demo

import androidx.fragment.app.FragmentManager
import com.yupao.block.cms.dialog.DialogConditionUtils
import com.yupao.block.cms.dialog.DialogFactory
import com.yupao.cms.dialog.QueryDialogUiStatus
import com.yupao.data.protocol.Resource
import com.yupao.model.cms.dialog.DialogConfigData
import com.yupao.model.cms.dialog.OnDialogClickListener
import kotlinx.coroutines.flow.Flow

/**
 * 用于向AI展示如何查询弹窗的配置
 *
 * 创建时间：2025/12/4
 *
 * @author fc
 */
object CheckDialogDemo {

    /**
     * 通知弹窗标识或者说ID来获取弹窗相关的数据：
     * 1. 是否能展示
     * 2. 弹窗的配置信息
     */
    fun queryDialog(identify: String?): Flow<Resource<QueryDialogUiStatus>> {
        return DialogConditionUtils.checkDialogByID(identify)
    }

    /**
     * 根据配置信息展示一个通用弹窗
     */
    fun showDialog(
        manager: FragmentManager?,
        dialogConfigData: DialogConfigData?,
        template: Map<String, String>?,
        isFormService: Boolean,
        pageCode: String?
    ) {
        manager ?: return
        dialogConfigData ?: return
        val dialog = DialogFactory.createDialog(
            dialogConfigData = dialogConfigData,
            isFormService = isFormService,
            pageCode = pageCode,
        ) {
            this.template = template
            this.buttonClickListener = OnDialogClickListener { btnIndex, path ->
                // 这里处理点击事件
                // true: 拦截关闭，false: 不拦截
                return@OnDialogClickListener true
            }
        }
        dialog.show(manager, dialogConfigData.dialogManagerBean.identify.orEmpty())
    }
}