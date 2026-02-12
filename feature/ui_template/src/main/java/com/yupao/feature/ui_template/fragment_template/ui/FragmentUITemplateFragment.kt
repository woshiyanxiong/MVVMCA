package com.yupao.feature.ui_template.fragment_template.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yupao.feature.ui_template.R
import com.yupao.feature.ui_template.databinding.UiTemplateFragmentFragmentUiTemplateBinding
import com.yupao.feature.ui_template.fragment_template.vm.FragmentUITemplateViewModel
import com.yupao.feature_block.android_ktx.lifecycle.collectEvent
import com.yupao.feature_block.status_ui.data_binding_utils.DataBindingManager
import com.yupao.feature_block.status_ui.status.ui.StatusUI
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 生成Fragment的模板代码示例
 *
 * 这个类是用于指导AI生成Fragment的模板代码，Fragment和Activity模板代码大体上差不多，其中vm和us基本一致。Fragment和Activity的差异主要在于参数提起、binding的初始化、对vm的数据观察、不需要设置toolbar上。
 *
 * 创建时间：2025/9/18
 *
 * @author fc
 */
@AndroidEntryPoint
class FragmentUITemplateFragment : Fragment() {

    @Inject
    lateinit var statusUI: StatusUI

    private val vm: FragmentUITemplateViewModel by viewModels<FragmentUITemplateViewModel>()

    private var binding: DataBindingManager<UiTemplateFragmentFragmentUiTemplateBinding>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingManager.inflateFragment<UiTemplateFragmentFragmentUiTemplateBinding>(
            layoutId = R.layout.ui_template_fragment_fragment_ui_template,
            inflater = inflater,
            container = container,
            owner = this,
        ) { binding ->
            binding.us = vm.us
        }.also { binding = it }.binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        extractParamsFromArgument()
    }

    private fun initObserver() {
        statusUI.addObserver(this, vm.status)
        // Fragment一般是没有错误视图的，这里只是表明可以这样用，在实际代码生成中，如果没有明确说明，则没有错误视图
        binding?.binding?.clRoot?.let { rootView ->
            statusUI.addErrorLayoutParent(parent = rootView) {
                vm.sync()
            }
        }
        viewLifecycleOwner.collectEvent(vm.openCitySelectorEvent) {
            openCitySelector()
        }
        viewLifecycleOwner.collectEvent(vm.openOccupationSelectorEvent) {
            openOccupationSelector()
        }
    }

    /**
     * 监听VM发出的打开城市选择器事件
     */
    private fun openCitySelector() {
        // 打开城市选择器，等待返回新的城市数据，然后调用vm的更新方法
        TODO()
    }

    /**
     * 监听VM发出的打开职位选择器事件
     */
    private fun openOccupationSelector() {
        // 打开职位选择器，等待返回新的职位数据，然后调用vm的更新方法
        TODO()
    }

    /**
     * 如果需要的话则从argument中提取参数，并调用vm的更新方法
     */
    private fun extractParamsFromArgument() {
        vm.sync()
    }
}