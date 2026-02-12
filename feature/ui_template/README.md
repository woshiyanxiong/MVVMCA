# 概述

此模块展示了目前项目中的最新架构代码，新生成的UI层代码应参照此模块代码。但项目还有一些按照老架构方式写的代码，这一点需要注意一下。

此模块代码不是业务代码，是为了用于辅助生成UI层的模板代码，目前包含的模板代码有：

1. Activity以及相关功能的代码，在com.yupao.feature.ui\_template.activity\_template目录下

2. Fragment以及相关功能的代码，在com.yupao.feature.ui\_template.fragment\_template目录下，这个还没写，先留白

3. Dialog以及相关功能的代码，在com.yupao.feature.ui\_template.dialog\_template目录下，这个还没写，先留白



注意：AI在生成代码时需要参考本说明和具体的目录下的代码来生成，代码中的注释补充完善了使用说明，不能忽略



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

   2. 构造器中注入业务数据，类型为Flow\<T>

   3. 通过监听业务数据来生成视图的状态

   4. US中研究修改业务数据

   5. US对外不提供方法调用，只提供类型为StateFlow\<T>的成员

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

8. ViewModel的写法在本模块中提供了详细的例子，请按照例子来



## FLOW相关封装说明

项目中原生开发涉及了大量使用kotlin flow的代码，为了方便大家使用，我们封装了一些api，在这做一些说明

### signalFlow

1. 用于声明一个信号，来触发后续操作，默认的写法为*signalFlow*\<T>()，如果没有参数T通常为Unit

2. *signalFlow*\<T>(true)是代表重新观察后会发射最新的值，但这种用法较少，一般用无参的



### nonNullStateIn

1. 实现为：fun \<T> Flow\<T>.nonNullStateIn(
   &#x20;   scope: CoroutineScope,
   &#x20;   value: T,
   &#x20;   started: SharingStarted = SharingStarted.Lazily
   ): StateFlow\<T> {
   &#x20;   return this.*stateIn*(scope, started, value)
   }

2. 用于快速生成StateFlow\<T>类型



### nullStateIn

1. 实现为：fun \<T> Flow\<T?>.nullStateIn(
   &#x20;   scope: CoroutineScope,
   &#x20;   started: SharingStarted = SharingStarted.Lazily,
   &#x20;   value: T? = null
   ): StateFlow\<T?> {
   &#x20;   return this.*stateIn*(scope, started, value)
   }

2. 用于快速生成StateFlow\<T?>类型



## Resource

1. Resource是用来封装数据请求过程，包含Resource.Loading,Resource.Error,Resource.Success

2. Resource常跟Flow一起使用，具体用法请参考本模块中的com.yupao.feature.ui\_template.resource\_demo.ResourceDemo类，该类列举了几个例子以供参考





## MutableResourceStatus

MutableResourceStatus是用来在VM中监听当前数据请求的状态，辅助视图来展示loading视图、弹一个Toast、或者展示错误弹窗

相关代码在feature\_block:status\_ui模块下的com.yupao.feature\_block.status\_ui.ktx目录

这里介绍一些常见用法：

1. Flow\<Resource\<T>>.*handleStatus*(\_status)，通过handleStatus操作符来让vm可以监听此次数据请求

```javascript
/**
 * 处理这个Flow的状态，把该流加进处理者中
 * @receiver Flow<Resource<T>>
 * @param status MutableResourceStatus? Flow状态处理者
 * @param isHandleLoading Boolean 是否处理Loading
 * @param isHandleError Boolean 是否处理错误
 * @return Flow<Resource<T>>
 */
fun <T> Flow<Resource<T>>.handleStatus(
    status: MutableResourceStatus?,
    isHandleLoading: Boolean = true,
    isHandleError: Boolean = true,
    errorInterceptor: (suspend (Resource.Error) -> Boolean)? = null
): Flow<Resource<T>> {
    if (status == null) return this
    return status.add(
        flow = this,
        isHandleLoading = isHandleLoading,
        isHandleError = isHandleError,
        errorInterceptor
    )
}
```

* \_status.tips(TipsEntity("更新成功"))，弹一个Toast

* 如果是需要拦截错误码，可以通过handleStatus中的errorInterceptor来拦截



## layout

1. 本项目中的视图还是通过xml来生成的

2. 需要使用DataBinding，默认根布局为androidx.constraintlayout.widget.ConstraintLayout

3. TextView要使用com.yupao.widget.text.YuPaoTextView

   1. 必须设置颜色，字号

   2. 行高通过app:ypTv\_lineHeightV2="18sp"来设置，大小单位跟字号使用的单位保持一致

   3. 加粗通过app:ypTv\_TextBold="true"来设置

4. ImageView使用androidx.appcompat.widget.AppCompatImageView

5. 一些带圆角的背景，通过使用cornersRadius="@{12f}"，solidColor="@{@color/black40}"这种来设置

6. 其他控件也优先使用androidx.appcompat.widget下的



# Activity模板使用指南

## 使用范围

当页面为带有路由的完整的一屏UI时，则根据Activity的模板来创建相关代码

## 目录结构

1. 父目录为：com.yupao.feature.模块名.页面名

2. 父目录下有三个子目录：entity、ui、vm

3. entity中放UI层需要的一些参数实体，比如页面的入参，一些页面属性的聚合，没有的话可以不生成这个目录

4. ui目录下放Activity文件

5. vm目录下放ViewModel和US



## 使用的技术栈

1. 使用Hilt来实现依赖注入

2. 使用flow来实现数据的传递

3. 在ViewModel中注入MutableResourceStatus来实现对数据加载状态的处理（展示loading和错误提示）

4. 使用DataBindingManager（使用DataBinding实现）来绑定视图和UIState



## Activity模板中各类的详细说明

### Activity

1. Activity对应Android官方概念中的Activity，需要继承om.yupao.page.BaseActivity

2. 通过@Route(path = "/ui\_template/page/activity\_ui\_template")代码来注册路由，路由的命名方式为：模块名/page/类名，注意这个类名需要去除Activity后缀并改为下划线命名

3. 由于使用了Hilt，Activity需要添加@AndroidEntryPoint，以表明其支持Hilt注入

4. 需要添加companion object，在此对象中定义一个静态的start方法

5. 注入一个com.yupao.feature\_block.status\_ui.status.ui.StatusUI对象，以让Activity支持处理VM中的请求状态的展示，如展示一个Loading或者进行错误提示

6. 在onCreate方法中进行初始化处理：com.yupao.page.set.ToolBarManager设置、view创建绑定、初始化观察者、从intent中提取出路由参数

7. 具体怎么实现，参考com.yupao.feature.ui\_template.activity\_template.ui.ActivityUITemplateActivity文件

8. 如果不需要自定义导航栏*ToolBarManager(activity = this, isNeedStatBar = true, isNeedToolBar = true)，如果需要则isNeedToolBar为false*

# Fragment模板使用指南

1. Fragment对应Android官方概念中的Fragment，直接继承官方组件Fragment

2. 由于使用了Hilt，Fragment需要添加@AndroidEntryPointt，以表明其支持Hilt注入

3. Fragment所使用的ViewModel，US这些跟Activity所使用的基本是一致的

4. 具体怎么实现，参考com.yupao.feature.ui\_template.fragment\_template.ui.FragmentUITemplateFragment文件



# Dialog模板使用指南

1. Dialog本质也是Fragment，继承BaseDialog2Fragment

2. 由于使用了Hilt，Dialog需要添加@AndroidEntryPointt，以表明其支持Hilt注入

3. 需要添加companion object，在此对象中定义一个静态的show方法

4. Dialog基本跟Fragment写法一致，一些差别参照com.yupao.feature.ui\_template.dialog\_template此文件夹下的文件

5. Dialog项目中主要分为从底部弹出和从页面中心出现，从底部弹出的参考com.yupao.feature.ui\_template.dialog\_template.bottom\_dialog.ui.BottomDialogUITemplateDialog。从中心出现的参考com.yupao.feature.ui\_template.dialog\_template.center\_dialog.ui.CenterDialogUITemplateDialog



# 项目中路由方案

1. 同模块之间通常直接通过Acitivty页面定义的start方法使用Intent直接进行跳转

2. 跨模块往往是feature模块会有一个对应的api模块，这个api模块会定义对应的实现模块对外暴露的跳转方法。然后发起跳转请求方会通过RouterApi.getByClass来获得api的实现实例后跳转

3. 最后一种是提供路由地址的字符串，然后通过RouterApi.runUri来跳转，这种情况是用于从后台、h5、rn等拿到路由地址来跳转的

4. com.yupao.feature.ui\_template.ui\_layer\_utils\_demo包下的RouterApiDemo展示了RouterApi的一些用法



# 弹窗系统

## 基本概念

项目中为了对弹窗进行统一管理，给每一个弹窗赋予了弹窗标识，我们可以通过该标识来查询弹窗是否展示以及查询弹窗的配置信息。如果弹窗没有标识说明没有接入统一管理中。弹窗还分为了自定义弹窗和通用弹窗。



## 在UI层中查询弹窗是否可用

1. 通过DialogConditionUtils的checkDialogByID方法可直接通过弹窗标识(ID)来查询弹窗的配置信息，包含了弹窗是否应该展示和用于弹窗展示的配置

2. 可通过CheckDialogDemo类的queryDialog方法查看示例



## 通用弹窗

1. 通用弹窗的样式是根据弹窗配置数据来展示，项目中提供了一些方法来通过弹窗标识展示一个通用弹窗。

2. CheckDialogDemo的showDialog展示了通过弹窗配置数据来展示一个通用弹窗，注意通过这种方式展示的弹窗已自动处理了埋点上报



## 自定义弹窗

1. 自定义的样式等由用户自己控制



# 在UI层进行埋点上报

## 弹窗埋点上报

1. 使用DialogPointerHelper类的dialogExposureByCode方法上报曝光

2. 使用DialogPointerHelper类的dialogCloseByCode方法上报关闭

3. 使用DialogPointerHelper类的dialogClickByCode方法上报点击

4. 注意上报点击是指点击按钮后上报，只要上报了点击事件就不报关闭事件了



## 其他埋点上报

```kotlin
val pointer = IPointerImpl(IM_FETCH_HISTORY_MSG_EXCEPTION)
pointer.addIntParam("code", error.code ?: 0)
pointer.addStringParam("desc", error.desc.orEmpty())
PointerApiFactory.instance.apiFindVolcengine().autoCommit(pointer)
```
