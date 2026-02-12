package com.yupao.feature.ui_template.activity_template.data

import com.yupao.data.protocol.Resource
import kotlinx.coroutines.flow.Flow

/**
 * 用户简历数据仓库，这个接口类只是为了示例才放在UI层，实际开发中，这个接口应该放在对应业务的data模块里，
 *
 * 创建时间：2025/9/16
 *
 * @author fc
 */
internal interface IResumeRep {
    /**
     * 检查用户简历是否存在
     */
    fun checkIsExistResume(): Flow<Resource<Boolean>>
}