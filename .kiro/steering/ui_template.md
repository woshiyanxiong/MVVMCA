---
inclusion: manual
---

# 概述

feature:ui_template 模块展示了目前项目中的最新架构代码，新生成的UI层代码应参照该模块代码。但项目还有一些按照老架构方式写的代码，这一点需要注意一下。

该模块代码不是业务代码，是为了用于辅助生成UI层的模板代码，目前包含的模板代码有：

1. Activity以及相关功能的代码，在com.yupao.feature.ui_template.activity_template目录下

2. Fragment以及相关功能的代码，在com.yupao.feature.ui_template.fragment_template目录下，这个还没写，先留白

3. Dialog以及相关功能的代码，在com.yupao.feature.ui_template.dialog_template目录下，这个还没写，先留白



注意：AI在生成代码时需要参考本说明和该模块具体目录下的代码来生成，代码中的注释补充完善了使用说明，不能忽略



# 技术栈

1. Kotlin语言

2. Hilt依赖注入

3. 使用了Kotlin的flow



# 通用的一些说明

## US

1. US是UIState的简称，但不同于项目中以UIState结尾的data class类

2. 以UIState结尾的data class 类只是单纯的描述了View视图的状态

3. 以US结尾的类是新架构新增的成员，该类不是data class类，为普通类，有以下几点特点：

   1. 同样对外暴露View视图的状态，

   2. 构造器中注入业务数据，类型为Flow<T>

   3. 通过监听业务数据来生成视图的状态

   4. US中研究修改业务数据

   5. US对外不提供方法调用，只提供类型为StateFlow<T>的成员

4. US通常是被ViewModel持有，来描述Activity或者Fragment页面的状态。一些比较简单的视图的依赖项可能只是简单的data class类型的UIState



## ViewModel

1. ViewModel对应Android官方概念中的ViewModel，需要继承androidx.lifecycle.ViewModel

2. ViewModel通常在Activity和Fragment的模板中使用，注意Dialog实际也为Fragment

3. 通过对类添加@HiltViewModel和对构造器标记@Inject来表明此ViewModel支持Hilt

4. 在构造器中注入MutableResourceStatus和业务仓库

5. 业务数据通过MutableStateFlow保存在ViewModel中，所有对业务数据的修改都限制在ViewModel中

6. 持有US对象对外暴露视图状态

7. 对外暴露的公共方法通常为三类：

   1. sync方法，用于初始化数据调用

   2. click方法，用于提供给视图的点击事件调用，通常为无参函数

   3. change方法或者说selected、update等，命名不定，用于视图层在执行了某些操作后将数据传给vm继续执行某些操作

8. ViewModel的写法在 feature:ui_template 模块中提供了详细的例子，请按照例子来



## FLOW相关封装说明

项目中原生开发涉及了大量使用kotlin flow的代码，为了方便大家使用，我们封装了一些api，在这做一些说明

### signalFlow

1. 用于声明一个信号，来触发后续操作，默认的写法为*signalFlow*<T>()，如果没有参数T通常为Unit

2. *signalFlow*<T>(true)是代表重新观察后会发射最新的值，但这种用法较少，一般用无参的



### nonNullStateIn

1. 实现为：fun <T> Flow<T>.nonNullStateIn(
    scope: CoroutineScope,
    value: T,
    started: SharingStarted = SharingStarted.Lazily
): StateFlow<T> {
    return this.*stateIn*(scope, started, value)
}

2. 用于快速生成StateFlow<T>类型



### nullStateIn

1. 实现为：fun <T> Flow<T?>.nullStateIn(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily,
    value: T? = null
): StateFlow<T?> {
    return this.*stateIn*(scope, started, value)
}

2. 用于快速生成StateFlow<T?>类型



## Resource

1. Resource是用来封装数据请求过程，包含Resource.Loading,Resource.Error,Resource.Success

2. Resource常跟Flow一起使用，具体用法请参考 feature:ui_template 模块中的com.yupao.feature.ui_template.resource_demo.ResourceDemo类，该类列举了几个例子以供参考
