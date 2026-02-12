package com.yupao.feature.ui_template.activity_template.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.yupao.feature.ui_template.R
import com.yupao.feature.ui_template.activity_template.vm.ActivityUITemplateViewModel
import com.yupao.feature.ui_template.databinding.UiTemplateActivityActivityUiTemplateBinding
import com.yupao.feature_block.android_ktx.lifecycle.collectEvent
import com.yupao.feature_block.status_ui.data_binding_utils.DataBindingManager
import com.yupao.feature_block.status_ui.status.ui.StatusUI
import com.yupao.page.BaseActivity
import com.yupao.page.set.ToolBarManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 生成Activity页面的模板代码示例
 *
 * 这个类是用于指导AI生成Activity页面的模板代码，使用的技术栈有Hilt依赖注入，DataBinding，Flow
 * 通过@Route注册页面的路由信息，但注意这个路由不能在项目中使用，项目中同模块通过start方法跳转，不同模块通过api封装来跳转
 *
 * 创建时间：2025/8/27
 *
 * @author fc
 */
@Route(path = "/ui_template/page/activity_ui_template")
@AndroidEntryPoint
internal class ActivityUITemplateActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ActivityUITemplateActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * 用于管理VM中的数据请求的状态处理，VM会对外暴露一个ResourceStatus，通过让statusUI监听ResourceStatus来展示
     * Loading实体和错误视图，以及支持弹Toast
     */
    @Inject
    lateinit var statusUI: StatusUI

    /**
     * ViewModel，用于处理业务逻辑，并返回给UI层进行展示,业务逻辑都在ViewModel中处理
     */
    private val vm: ActivityUITemplateViewModel by viewModels<ActivityUITemplateViewModel>()

    /**
     * DataBindingManager，用于管理DataBinding，使用DataBindingManager来加载页面内容，并将view视图与其需要的UIState
     * 和Action进行绑定，从而实现页面的更新
     */
    private var binding: DataBindingManager<UiTemplateActivityActivityUiTemplateBinding>? = null

    /**
     * 在onCreate方法中进行初始化处理：ToolBarManager设置、view创建绑定、初始化观察者、从intent中提取出路由参数
     * 1.ToolBarManager中包含了对于顶部状态栏（电量，时间等这一栏），App内的顶部导航栏（展示页面的标题和返回键）
     * 2.没有特殊说明的情况下，使用ToolBarManager(activity = this, isNeedStatBar = true, isNeedToolBar = true)，其中
     * isNeedToolbar表示需要app内导航栏，如果有特殊说明使用自定义导航栏，这个值为false
     * 然后在xml中增加导航栏的布局
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToolBarManager(activity = this, isNeedStatBar = true, isNeedToolBar = true).apply {
            initToolBar(title = "Activity模板", isToolBarColorWhite = true)
            setStateBarColorInt(Color.WHITE)
            setStateBarMode(true)
        }
        initViewContent()
        initObserver()
        extractParamsFromIntent()
    }

    /**
     * 初始化加载页面内容，使用已封装好的工具类DataBindingManager来进行视图的加载，并将view视图与其需要的UIState和Action进行绑定，
     * 此部分代码为固定写法，仅需替换参数
     */
    private fun initViewContent() {
        DataBindingManager.inflateActivity<UiTemplateActivityActivityUiTemplateBinding>(
            R.layout.ui_template_activity_activity_ui_template,
            this
        ) { binding ->
            binding.us = vm.us
            binding.action = vm
        }.also {
            binding = it
        }
    }

    /**
     * 初始化观察者，用于处理事件等，需要处理的任务有：
     * 1. 监听ResourceStatus，并使用StatusUI来处理Loading实体和错误视图，以及支持弹Toast
     * 2. 通过addErrorLayoutParent错误视图的重试点击事件来调用vm的sync方法，注意不需要错误视图时可以不要这个
     * 3. 通过collectEvent来监听vm发出的类似为Event的视图事件，
     * 4. 通过collectFlow来监听vm对外暴露的数据，但因为一般情况是通过databinding来处理的，所以大部分情况下不需要
     * 5. 注意所有collect中的实际处理代码应单独提取成一个方法，initObserver这个方法的代码量过大
     * 6. 此方法的实现在实际代码生成中应进行替换，不要照搬，而是根据实际情况进行修改
     * 7. collectEvent和collectFlow为项目中我们自己封装的
     */
    private fun initObserver() {
        statusUI.addObserver(this, vm.status)
        binding?.binding?.clRoot?.let { rootView ->
            statusUI.addErrorLayoutParent(parent = rootView) {
                vm.sync()
            }
        }
        collectEvent(vm.openCitySelectorEvent) {
            openCitySelector()
        }
        collectEvent(vm.openOccupationSelectorEvent) {
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
     * 如果需要的话则从Intent中提取参数来初始化数据，由于此页面没有从Intent中提取参数，所以此处只调用初始化同步数据方法即可
     */
    private fun extractParamsFromIntent() {
        vm.sync()
    }
}