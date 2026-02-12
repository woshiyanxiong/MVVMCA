package com.yupao.feature.ui_template.dialog_template.center_dialog.ui

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
import com.yupao.feature.ui_template.databinding.UiTemplateDialogCenterDialogUiTemplateBinding
import com.yupao.feature.ui_template.dialog_template.center_dialog.entity.CenterDialogUITemplateParams
import com.yupao.feature.ui_template.dialog_template.center_dialog.vm.CenterDialogUITemplateViewModel
import com.yupao.feature_block.android_ktx.lifecycle.collectEvent
import com.yupao.feature_block.status_ui.data_binding_utils.DataBindingManager
import com.yupao.feature_block.status_ui.di.FragmentResourceStatusScope
import com.yupao.feature_block.status_ui.status.ui.StatusUI
import com.yupao.page.BaseDialog2Fragment
import com.yupao.page.setCenterInAnim
import com.yupao.page.setLayoutParams
import com.yupao.page.setOnlyCenterOutAnim
import com.yupao.utils.system.window.DensityUtils
import com.yupao.utils.system.window.ScreenTool
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 从页面中部弹出的弹窗页面模板代码示例，弹窗本质是个fragment，所以跟fragment的写法差不多，像vm和us等跟Activity的写法没啥区别。
 * 注意弹窗页面：1.增加页面曝光、页面关闭、页面点击事件的埋点 2.要设置是底部还是中间，本类展示的是从中部弹出的弹窗
 *
 * 创建时间：2025/9/29
 *
 * @author fc
 */
@AndroidEntryPoint
class CenterDialogUITemplateDialog : BaseDialog2Fragment() {
    companion object {
        fun show(
            manager: FragmentManager?,
            params: CenterDialogUITemplateParams? = null,
        ) {
            manager ?: return
            CenterDialogUITemplateDialog().also { dialog ->
                dialog.params = params
            }.show(manager, "CenterDialogUITemplateDialog")
        }
    }

    /**
     * 弹窗参数
     */
    private var params: CenterDialogUITemplateParams? = null

    /**
     * 弹窗页面数据绑定
     */
    private var binding: DataBindingManager<UiTemplateDialogCenterDialogUiTemplateBinding>? = null

    /**
     * 弹窗的vm
     */
    private val vm by viewModels<CenterDialogUITemplateViewModel>()

    /**
     * 由于弹窗的window是单独的，不是使用的Activity的，因此statusUI需要通过@FragmentResourceStatusScope注解获取
     */
    @Inject
    @FragmentResourceStatusScope
    lateinit var statusUI: StatusUI

    override fun onCreate(savedInstanceState: Bundle?) {
        // 通过扩展函数setOnlyCenterOutAnim把弹窗style设置为从页面中心出现，通过这种方式会清除掉原本的弹窗进入动画，只保留弹窗退出动画，这是符合预期的。
        setOnlyCenterOutAnim()
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
        // 在此通过setLayoutParams设置dialog窗口的layout参数，从中部弹出的弹窗通常宽度为固定值，高度为wrap_content
        dialog.setLayoutParams(
            Gravity.CENTER,
            ScreenTool.getScreenWidth(requireContext()) - DensityUtils.dp2px(requireContext(), 62f),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 从页面中心弹出的弹窗需要通过setCenterInAnim扩展函数设置一个进入动画，因为在onCreate设置的style只有窗口退出动画，没有进入的，这样设置的目的是为了解决当弹窗高度为wrap时，进入动画会失效的问题
        dialog.setCenterInAnim()
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingManager.inflateFragment<UiTemplateDialogCenterDialogUiTemplateBinding>(
            R.layout.ui_template_dialog_center_dialog_ui_template,
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