package com.yupao.feature.ui_template.dialog_template.bottom_dialog.ui

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.yupao.block.cms.pointer.dialog.DialogPointerHelper
import com.yupao.feature.ui_template.R
import com.yupao.feature.ui_template.databinding.UiTemplateDialogBottomDialogUiTemplateBinding
import com.yupao.feature.ui_template.dialog_template.bottom_dialog.entity.BottomDialogUITemplateParams
import com.yupao.feature.ui_template.dialog_template.bottom_dialog.vm.BottomDialogUITemplateViewModel
import com.yupao.feature_block.android_ktx.lifecycle.collectEvent
import com.yupao.feature_block.status_ui.data_binding_utils.DataBindingManager
import com.yupao.feature_block.status_ui.di.FragmentResourceStatusScope
import com.yupao.feature_block.status_ui.status.ui.StatusUI
import com.yupao.page.BaseDialog2Fragment
import com.yupao.page.setBottomInAnim
import com.yupao.page.setLayoutParams
import com.yupao.page.setOnlyBottomOutAnim
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 从页面底部弹出的弹窗页面模板代码示例，弹窗本质是个fragment，所以跟fragment的写法差不多，像dialog使用的vm和us等跟Activity的写法没啥区别。
 * 注意弹窗页面：1.增加页面曝光、页面关闭、页面点击事件的埋点 2.要设置是底部还是中间，本类展示的是从底部弹出的弹窗
 *
 * 创建时间：2025/9/29
 *
 * @author fc
 */
@AndroidEntryPoint
class BottomDialogUITemplateDialog : BaseDialog2Fragment() {
    companion object {
        /**
         * 通过调用show方法来展示弹窗
         */
        fun show(
            manager: FragmentManager?,
            params: BottomDialogUITemplateParams? = null,
        ) {
            manager ?: return
            BottomDialogUITemplateDialog().also { dialog ->
                dialog.params = params
            }.show(manager, "BottomDialogUITemplateDialog")
        }
    }

    /**
     * 弹窗参数
     */
    private var params: BottomDialogUITemplateParams? = null

    /**
     * 弹窗页面数据绑定
     */
    private var binding: DataBindingManager<UiTemplateDialogBottomDialogUiTemplateBinding>? = null

    /**
     * 弹窗的vm
     */
    private val vm by viewModels<BottomDialogUITemplateViewModel>()

    /**
     * 由于弹窗的window是单独的，不是使用的Activity的，因此statusUI需要通过@FragmentResourceStatusScope注解获取
     */
    @Inject
    @FragmentResourceStatusScope
    lateinit var statusUI: StatusUI

    override fun onCreate(savedInstanceState: Bundle?) {
        // 通过扩展函数setOnlyBottomOutAnim把弹窗style设置为从底部弹入，通过这种方式会清除掉原本的弹窗进入动画，只保留弹窗退出动画，这是符合预期的。
        setOnlyBottomOutAnim()
        super.onCreate(savedInstanceState)
        // 本项目中的dialog如果是重建的，一律关闭
        if (savedInstanceState != null) {
            kotlin.runCatching {
                dismissAllowingStateLoss()
            }
            return
        }
        pointExposureEvent()
    }

    /**
     * 弹窗曝光埋点，由于业务要求只上报一次即可，所以在onCreate中调用此方法
     */
    private fun pointExposureEvent() {
        // 弹窗页面曝光埋点，弹窗的曝光埋点必须要有弹窗标识，没有弹窗标识的不用埋
        DialogPointerHelper.dialogExposureByCode(
            identity = vm.dialogID,
            pageCode = params?.pageCode,
            reportService = true
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 在此通过setLayoutParams设置dialog窗口的layout参数，从底部弹出的弹窗通常宽度为match，高度为wrap_content
        dialog.setLayoutParams(
            Gravity.BOTTOM,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 从底部弹出的弹窗需要通过setBottomInAnim扩展函数设置一个进入动画，因为在onCreate设置的style只有窗口退出动画，没有进入的，这样设置的目的是为了解决当弹窗高度为wrap时，进入动画会失效的问题
        dialog.setBottomInAnim()
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingManager.inflateFragment<UiTemplateDialogBottomDialogUiTemplateBinding>(
            R.layout.ui_template_dialog_bottom_dialog_ui_template,
            inflater,
            container,
            viewLifecycleOwner
        ) { binding ->
            binding.us = vm.us
        }.also {
            binding = it
        }.binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusUI.setLifecycleOwner(viewLifecycleOwner)
        statusUI.addObserver(viewLifecycleOwner, vm.status)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        initObserver()
        vm.sync(this.params)
    }

    private fun initObserver() {
        viewLifecycleOwner.collectEvent(vm.closeEvent) {
            pointCloseEvent()
            dismissAllowingStateLoss()
        }
    }

    /**
     * 对点击关闭按钮进行埋点
     */
    private fun pointCloseEvent() {
        DialogPointerHelper.dialogCloseByCode(
            identity = vm.dialogID,
            pageCode = params?.pageCode,
            reportService = true
        )
    }
}