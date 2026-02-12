package com.yupao.feature.ui_template.fragment_template.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yupao.data.protocol.Resource
import com.yupao.feature.ui_template.activity_template.data.CityEntity
import com.yupao.feature.ui_template.activity_template.data.IPushSettingRep
import com.yupao.feature.ui_template.activity_template.data.IResumeRep
import com.yupao.feature.ui_template.activity_template.data.IUserSubscribeRep
import com.yupao.feature.ui_template.activity_template.data.OccupationEntity
import com.yupao.feature.ui_template.activity_template.data.UserSubscribeEntity
import com.yupao.feature.ui_template.fragment_template.ui_state.FragmentUITemplateUS
import com.yupao.feature_block.status_ui.ktx.asResourceStatus
import com.yupao.feature_block.status_ui.ktx.handleResultStatus
import com.yupao.feature_block.status_ui.ktx.handleStatus
import com.yupao.feature_block.status_ui.status.MutableResourceStatus
import com.yupao.feature_block.status_ui.status.TipsEntity
import com.yupao.kit.kotlin.signalFlow
import com.yupao.model.event.EventData
import com.yupao.scafold.ktx.combineResource
import com.yupao.scafold.ktx.onEachSuccess
import com.yupao.scafold.ktx.transformFlowResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

/**
 * Fragment使用的ViewModel和Activity使用的ViewModel没有什么区别，所以这里的代码是直接从ActivityUITemplateViewModel类拷贝过来的
 * 使用的方法和规范都按照ActivityUITemplateViewModel中描述的来
 *
 * 创建时间：2025/9/19
 *
 * @author fc
 */
@HiltViewModel
internal class FragmentUITemplateViewModel @Inject constructor(
    private val _status: MutableResourceStatus,
    private val rep: IUserSubscribeRep,
    private val resumeRep: IResumeRep,
    private val pushSettingRep: IPushSettingRep,
) : ViewModel() {

    /**
     * 业务数据存储，如果业务数据比较复杂或者涉及各种不同的业务数据，也可以分开存储
     */
    private val userSubscribeData: MutableStateFlow<UserSubscribeEntity?> = MutableStateFlow(null)

    /**
     * 业务数据存储，用于存储是否存在简历，一般分属于不同业务域的数据我们分开存储
     */
    private val isExistResume: MutableStateFlow<Boolean?> = MutableStateFlow(null)

    /**
     * 业务数据存储，用于存储语音播报开关状态
     */
    private val isEnableVoiceBroadcast: MutableStateFlow<Boolean?> = MutableStateFlow(null)

    /**
     * 更新本地业务数据的方法，此方法应根据实际代码修改或者删除，不是一定有的代码
     */
    private fun updateDesiredCityCache(cities: List<CityEntity>?) {
        val userSubscribeData = userSubscribeData.value ?: return
        val newUserSubscribeData = userSubscribeData.copy(city = cities)
        this.userSubscribeData.value = newUserSubscribeData
    }

    /**
     * 更新本地业务数据的方法，此方法应根据实际代码修改或者删除，不是一定有的代码
     */
    private fun updateDesiredOccupationCache(
        fullTimeOccupation: List<OccupationEntity>?,
        partTimeOccupationEntity: List<OccupationEntity>?
    ) {
        val userSubscribeData = userSubscribeData.value ?: return
        val newUserSubscribeData = userSubscribeData.copy(
            fullTimeOccupationEntity = fullTimeOccupation,
            partTimeOccupationEntity = partTimeOccupationEntity
        )
        this.userSubscribeData.value = newUserSubscribeData
    }

    /**
     * 更新本地业务数据的方法，此方法应根据实际代码修改或者删除，不是一定有的代码
     */
    private fun updateIsEnableSubscribeCache(isEnable: Boolean) {
        val userSubscribeData = userSubscribeData.value ?: return
        val newUserSubscribeData = userSubscribeData.copy(isEnable = isEnable)
        this.userSubscribeData.value = newUserSubscribeData
    }

    private val syncSignal = signalFlow<Unit>()

    /**
     * 同步本页面展示需要的业务数据，本段代码展示的逻辑为先检查用户是否存简历，不存在则不需要再获取订阅数据了。
     * 如果存在再获取订阅数据，则使用combineResource扩展函数来同时拉取用户订阅数据和语音播报开关状态。
     * 在实际需求代码中生成中如果遇见要求先校验一个数据，如果成功了在获取其他数据时，就参照这个逻辑来编写
     * 一般情况下一个Flow<Resource<*>>中只存在一个handleStatus和handleResultStatus,通常就放在最末尾
     */
    private val handleSyncSignal = syncSignal.flatMapLatest {
        resumeRep.checkIsExistResume().onEachSuccess {
            isExistResume.value = it.data
        }.transformFlowResource { isExistResume ->
            if (isExistResume != true) return@transformFlowResource flowOf(Resource.Success(Unit))
            rep.getUserSubscribeData()
                .combineResource(pushSettingRep.queryIsEnableVoiceBroadcast()) { subscribeData, isEnable ->
                    Pair(subscribeData, isEnable)
                }.onEachSuccess { result ->
                    val (subscribeData, isEnable) = result.data ?: return@onEachSuccess
                    userSubscribeData.value = subscribeData
                    isEnableVoiceBroadcast.value = isEnable
                }
        }
    }.handleStatus(_status).handleResultStatus(_status)

    /**
     * 更新用户的期望城市，像这种提交数据给后台的方法，成功后一般直接修改ViewModel中缓存的业务数据，而不用再去调用sync方法
     */
    private val updateCitySignal = signalFlow<List<CityEntity>?>()
    private val handleUpdateCitySignal = updateCitySignal.flatMapLatest { cities ->
        rep.updateDesiredCity(cities).onEachSuccess {
            updateDesiredCityCache(cities)
        }
    }.handleStatus(_status)

    /**
     * 此方法中使用了combineResource，当需要同时调用调用两个仓库的方法时，且这两个方法返回的都为Flow<Resource<*>>，
     * 可以使用combineResource
     */
    private val updateOccupationSignal =
        signalFlow<Pair<List<OccupationEntity>?, List<OccupationEntity>?>>()
    private val handleUpdateOccupationSignal = updateOccupationSignal.flatMapLatest {
        val (fullTimeOccupation, partTimeOccupationEntity) = it
        rep.updateFullTimeDesiredOccupation(fullTimeOccupation)
            .combineResource(rep.updatePartTimeDesiredOccupation(partTimeOccupationEntity)) { _, _ ->
                Unit
            }.onEachSuccess {
                updateDesiredOccupationCache(fullTimeOccupation, partTimeOccupationEntity)
            }
    }.handleStatus(_status)

    /**
     * 更新用户的订阅开关
     */
    private val updateSubscribeSwitchSignal = signalFlow<Boolean>()
    private val handleUpdateSubscribeSwitchSignal =
        updateSubscribeSwitchSignal.flatMapLatest { isEnable ->
            rep.updateSubscribeSwitch(isEnable).onEachSuccess {
                // 提示更新成功Toast
                _status.tips(TipsEntity("更新成功"))
                updateIsEnableSubscribeCache(isEnable)
            }
        }.handleStatus(_status)

    /**
     * 用于请求数据中的状态管理
     */
    val status = _status.asResourceStatus()

    /**
     * 暴露给UI使用的UIState,构造器中要显示写出参数名
     */
    val us = FragmentUITemplateUS(
        userSubscribeData = userSubscribeData,
        voiceBroadcastSwitchData = isEnableVoiceBroadcast,
        scope = viewModelScope
    )

    /**
     * 打开城市选择器的事件，在实际代码生成中按照这种格式来写，命名为xxxEvent，并且使用EventData来封装
     */
    private val _openCitySelectorEvent = signalFlow<EventData<Unit>>()
    val openCitySelectorEvent = _openCitySelectorEvent.asSharedFlow()

    /**
     * 打开职位选择器的事件，在实际代码生成中按照这种格式来写，命名为xxxEvent，并且使用EventData来封装
     */
    private val _openOccupationSelectorEvent = signalFlow<EventData<Unit>>()
    val openOccupationSelectorEvent = _openOccupationSelectorEvent.asSharedFlow()

    /**
     * 处理用户点击了编辑城市按钮
     */
    fun clickEditCity() {
        _openCitySelectorEvent.tryEmit(EventData(Unit))
    }

    /**
     * 处理用户点击了编辑职位按钮
     */
    fun clickEditOccupation() {
        _openOccupationSelectorEvent.tryEmit(EventData(Unit))
    }

    /**
     * 处理用户点击了订阅开关
     */
    fun clickSubscribeSwitch() {
        val isEnable = userSubscribeData.value?.isEnable != true
        updateSubscribeSwitchSignal.tryEmit(isEnable)
    }

    /**
     * 同步用户的职位订阅数据
     */
    fun sync() {
        syncSignal.tryEmit(Unit)
    }

    /**
     * 用户选择了新的期望城市，当用户从选择器选完后，调用此方法
     */
    fun changeDesiredCity(cities: List<CityEntity>?) {
        updateCitySignal.tryEmit(cities)
    }

    /**
     * 用户选择了新的期望职位，当用户从选择器选完后，调用此方法
     */
    fun changeDesiredOccupation(
        fullTimeOccupation: List<OccupationEntity>?,
        partTimeOccupationEntity: List<OccupationEntity>?
    ) {
        updateOccupationSignal.tryEmit(Pair(fullTimeOccupation, partTimeOccupationEntity))
    }

    init {
        handleSyncSignal.launchIn(viewModelScope)
        handleUpdateCitySignal.launchIn(viewModelScope)
        handleUpdateOccupationSignal.launchIn(viewModelScope)
        handleUpdateSubscribeSwitchSignal.launchIn(viewModelScope)
    }
}