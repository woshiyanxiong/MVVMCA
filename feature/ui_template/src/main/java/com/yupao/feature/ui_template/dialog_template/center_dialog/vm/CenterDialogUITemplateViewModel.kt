package com.yupao.feature.ui_template.dialog_template.center_dialog.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yupao.feature.ui_template.dialog_template.center_dialog.entity.CenterDialogUITemplateParams
import com.yupao.feature.ui_template.dialog_template.center_dialog.ui_state.CenterDialogUITemplateUS
import com.yupao.feature_block.status_ui.ktx.asResourceStatus
import com.yupao.feature_block.status_ui.status.MutableResourceStatus
import com.yupao.kit.kotlin.signalFlow
import com.yupao.model.event.EventData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/**
 * dialog使用的ViewModel与Activity使用的ViewModel在写法上没有差别，直接参考ActivityUITemplateViewModel的规范来写就行
 *
 * 创建时间：2025/9/29
 *
 * @author fc
 */
@HiltViewModel
internal class CenterDialogUITemplateViewModel @Inject constructor(
    private val _status: MutableResourceStatus
) : ViewModel() {
    private var params: CenterDialogUITemplateParams? = null
    val status = _status.asResourceStatus()

    private val _closeEvent = signalFlow<EventData<Unit>>()
    val closeEvent = _closeEvent.asSharedFlow()

    // 弹窗标识
    val dialogID = "xxxxxxx"

    val us = CenterDialogUITemplateUS(viewModelScope)

    fun sync(params: CenterDialogUITemplateParams?) {
        this.params = params
        TODO()
    }
}