package com.yupao.feature.ui_template.resource_demo

import com.yupao.data.protocol.Resource
import com.yupao.scafold.ktx.combineResource
import com.yupao.scafold.ktx.combineResourceTransform
import com.yupao.scafold.ktx.completeOrNull
import com.yupao.scafold.ktx.mapNetResource
import com.yupao.scafold.ktx.mapResource
import com.yupao.scafold.ktx.resultOrNull
import com.yupao.scafold.ktx.transformFlowResource
import com.yupao.scafold.ktx.transformFlowResourcePair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 在ViewModel中会调用仓库方法来获取数据，项目中的仓库往往返回的Flow<Resource<*>>，Resource是我们自己封装的类型，并且
 * 提供了很多扩展函数，方便使用，本类就是用来展示一些常规用法。关于Resource和相关扩展函数的源码在data_protocol模块中，
 * 该模块为远程仓库的封装，通过com.yupao.data:data_protocol依赖引入 。
 * 本类简单介绍了几种扩展函数的用法，其他用法参考源码
 *
 * 创建时间：2025/9/16
 *
 * @author fc
 */
internal object ResourceDemo {

    /**
     * 示例1，此方法展示了一个业务逻辑：先拿到用户id，然后根据用户id去获取用户信息，如果用户id为空，则返回错误信息。
     * 即先调用一个仓库方法，如果调用失败了则不需要继续调用。如果成功了，则继续调用另外一个仓库方法，只要是这种逻辑就可以用这种方式
     *
     * @param f1
     * @param f2
     */
    fun demo1(f1: Flow<Resource<String>>, f2: Flow<Resource<UserInfo>>): Flow<Resource<UserInfo>> {
        // 这个方法的返回值只有f2的值
        val fetchUserInfo = f1.transformFlowResource { id ->
            if (id.isNullOrBlank()) return@transformFlowResource flowOf(Resource.Error(errorMsg = "获取到的id为空"))
            f2
        }
        // 这个方法的返回值是一个Pair对象，会同时返回f1和f2的值
        val fetchUserInfo2 = f1.transformFlowResourcePair { id ->
            if (id.isNullOrBlank()) return@transformFlowResourcePair flowOf(Resource.Error(errorMsg = "获取到的id为空"))
            f2
        }
        return fetchUserInfo
    }

    /**
     * 示例2，此方法展示了同时请求多个仓库方法，且这些方法都返回Flow<Resource<*>>时的一些扩展方法的用法
     */
    fun demo2(f1: Flow<Resource<String>>, f2: Flow<Resource<Boolean>>, f3: Flow<Resource<Int>>) {
        // 同时请求两个
        f1.combineResource(f2) { value1, value2 ->
            // 拿到两个仓库方法返回的Resource，然后进行逻辑处理
        }
        // 同时请求三个
        f1.combineResource(f2, f3) { value1, value2, value3 ->
            // 拿到三个仓库方法返回的Resource，然后进行逻辑处理
        }
        // combineResource方法最多可接受7个参数，如果超过7个，请使用下面的方式
        combineResourceTransform(false, f1, f2, f3) { valueArray ->
            val value1 = valueArray[0] as String
            val value2 = valueArray[1] as Boolean
            val value3 = valueArray[2] as Int
        }
    }

    /**
     * 示例3，此方法展示了调用了一个仓库方法，且该方法返回的类型为Flow<Resource<T>>,将该方法转换为另外一个类型Flow<Resource<R>>
     * result1和result2都将Int转为了String，它们的区别在于，mapResource仅当f1返回的是Resource.Success时才会执行转换逻辑.
     * 而result2在此基础上，如果返回的Resource.Error，则尝试从Error中取出data，对取出的data也会尝试进行转换，当然此种情况最终还是Resource.Error。
     */
    fun demo3(f1: Flow<Resource<Int>>) : Flow<Resource<String>> {
        val result1 = f1.mapResource { value ->
            value?.toString()
        }
        val result2 = f1.mapNetResource { value ->
            value?.toString()
        }
        return result2
    }

    /**
     * 示例4，此方法展示了如何在协程环境中直接拿到Flow<Resource<*>>的值
     */
    suspend fun demo4(f1: Flow<Resource<String>>): String {
        // resultOrNull()方法返回的数据是拆箱过的，即String?，如果是Error则直接返回null
        val result = f1.resultOrNull()
        // completeOrNull方法返回的是一个Resource.Success对象，用于知道本次请求是否成功了
        val result2 = f1.completeOrNull()
        return result.orEmpty()
    }
}

@SuppressWarnings("DataKeepRules", "DataClassFieldRule")
internal data class UserInfo(val name: String, val age: Int)