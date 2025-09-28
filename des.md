# 什么是MVVM

基本上很多开发人员都被问过MVC、MVP、MVVM之间的区别吧。在回答这一堆专业术语的背后是否想过它们到底为什么服务？

它们的区别这里不做介绍了，现在来拆分一下MVVM。即**Model**、**ViewModel**、**View**。嗯？这不都知道吗？需要来脱裤子放屁多此一举？NONONO。这里我们来深度解析一下这背后到底代表着什么。

## MVVM核心概念

- **Model** → 数据，来自网络接口即后台接口的数据 和 我们需要保存到本地的数据
- **ViewModel** → 我们暂时跳出Android中的ViewModel组件。这里也是数据，属于View的数据
- **View** → 界面以及相关UI元素

## 数据驱动UI的本质

回想一下客户端的交互：页面信息的加载 → 用户浏览、点击、刷新。这个操作本质上就是**数据的交互**，数据和UI、用户的交互。

这个交互更深层的逻辑是：**数据驱动UI**。

> 当你吃透了这六个字，恭喜你，你已经学会了大前端。

## 实际案例分析

我们来举个例子。在社区发布一个信息帖子到其他用户在社区里面看到该信息，会经历以下几个状态：

- **审核中**
- **审核失败** 
- **通过**

这个时候后台返回一个状态值 `infoState` 用 `0`、`1`、`2` 来表示这几种状态。产品根据这三种状态来展示3种不同的UI界面。

在以上情况下你们一般会怎么实现？现在我们来通过数据实体来模拟这个交互。

### 数据层设计

首先我们建立网络数据实体：

```kotlin
data class InfoNetModel(
    val infoState: Int?
){
    fun isReviewing():Boolean{
        return infoState==1
    }
    fun isRejected():Boolean{
        return infoState==2
    }
    fun isApproved():Boolean{
        return infoState==0
    }
}
```

然后我们建立UI实体：

```kotlin
data class InfoUIState(
    val isReviewing: Boolean = false,    // 审核中
    val isRejected: Boolean = false,     // 审核失败
    val isApproved: Boolean = false,     // 通过
    val showRetryButton: Boolean = false // 是否显示重试按钮
    
)
```

### ViewModel数据转换

```kotlin
class InfoViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(InfoUIState())
    val uiState: StateFlow<InfoUIState> = _uiState.asStateFlow()
    
    fun loadInfo() {
        // 模拟网络请求
        val netData = InfoNetModel(infoState = 1) // 假设返回审核失败
        
        // 数据转换：Model → ViewModel
        _uiState.value = when (netData.infoState) {
            it.isReviewing() -> InfoUIState(isReviewing = true)
            it.isRejected() -> InfoUIState(isRejected = true, showRetryButton = true)
            it.isApproved() -> InfoUIState(isApproved = true)
            else -> InfoUIState()
        }
    }
}
```
这就是一个很简单的 数据转view数据到作用到view，看起开简单，但是在实际的项目开发中一个
功能是很庞大的，存在多种状态以及对应的ui、交互等等。这个时候处理好数据转换的边界是比较重要的
这决定了后续的维护与扩展。举个例子，在开发中是否遇到过因为某种不可抗力的原因
后台需要更改数据字段，以及部分数据格式。如果有回想一哈最后怎么处理的？当然最好的处理是将修改控制到数据层


这里不在过多介绍了几句话也说不完。感兴趣的直接去看官方文档




### View层响应

```kotlin
@Composable
fun InfoScreen(viewModel: InfoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 数据驱动UI渲染
    when {
        uiState.isReviewing -> {
            ReviewingUI()
        }
        uiState.isRejected -> {
            RejectedUI(
                showRetryButton = uiState.showRetryButton,
                onRetry = { viewModel.loadInfo() }
            )
        }
        uiState.isApproved -> {
            ApprovedUI()
        }
    }
}
```
# 组件化架构设计

## 什么是组件化？

到底什么是组件化呢？很多人都回答到一个东西'独立编译'，这个东西到底好用不好用。多少场景能用的到？特别是在需要登陆状态的时候。我想用过的都知道这个到底适合在什么样的应用场景，这里不在拓展了。

## 组件化面临的核心问题

组件化一定会面临的一个问题。那就是各个组件如何通信？我想一定遇到过这样的场景A业务块下面有个弹框。过了一段时间B业务快也需要使用这个弹框。那么这个时候一般是怎么做的？下层到公共模块里面比如common包。如果这个弹框还涉及到接口与关联的业务逻辑，那么相关的也得下层。久而久之这个common是不是就变味有点不对劲？

### 解决方案：接口通信

那怎么通信呢？很简单通过接口通信，在直白一点就是为每个业务块开放一个api 只提供对外接口和实体。实现在业务快里面。这个操作通过ARouter 或者dagger 来实现。

## 组件划分原则

另一方面组件划分问题，也就是边界值。分好了是组件 分不好就是模块，大多数情况下组件的划分为：

### 基础组件化结构

```
MyApp/
├── app/                       # 主工程，一个空的"壳"
│   └── 主要职责：集成所有组件，初始化全局配置（如路由）
├── module_home/              # 首页组件（可独立运行）
├── module_video/             # 视频组件（可独立运行）
├── module_shopping/          # 商城组件（可独立运行）
├── module_user/              # 用户中心组件（可独立运行）
├── library_base/             # 基础组件
│   ├── 基类（BaseActivity, BaseFragment）
│   ├── 通用工具类（ToastUtils, LogUtils）
│   └── 公共资源（colors, dimens）
└── library_common/           # 公共业务组件
    ├── 网络请求封装
    ├── 图片加载封装
    ├── 路由 SDK 的封装和配置
    └── 第三方 SDK 的统一管理
```

## 数据层的重要性

其余我们还需要很重要的模块那就是数据data层。业务组件（如 feature_home）不关心数据来自网络、数据库还是缓存。它只通过 Repository 获取数据。Repository 决定从哪里获取数据以及如何缓存，同时每个业务线的数据独立。其他业务线需要引用只需要引用对于的data模块就行。

### 完整的组件化架构

```
MyApp/
├── app/                       # 主工程/壳工程
│   └── 职责：集成所有组件，初始化全局配置（路由、AppContext等）
│
├── feature_home/              # 首页业务组件（可独立运行）
├── feature_video/             # 视频业务组件（可独立运行）
├── feature_shopping/          # 商城业务组件（可独立运行）
│
├── library_base/              # 基础组件
│   ├── 基类（BaseActivity, BaseFragment）
│   ├── 通用工具类（ToastUtils, LogUtils）
│   └── 公共资源（colors, dimens）
│
├── library_common/            # 公共业务组件
│   ├── 网络请求封装
│   ├── 图片加载封装
│   └── 路由 SDK 的封装和配置
│
└── data/                      # 数据层（核心新增）
    ├── repository/            # 仓库层
    │   ├── HomeRepository.java
    │   ├── VideoRepository.java
    │   └── ... (实现接口，合并本地和远程数据源)
    │
    ├── model/                 # 模型层/实体层
    │   ├── api/               # 网络接口返回的数据模型
    │   ├── db/                # 数据库对应的数据模型
    │   └── ui/                # UI 层使用的数据模型（可选，可与 api/db 不同）
    │
    ├── source/                # 数据源层
    │   ├── remote/            # 远程数据源
    │   │   ├── api/           # Retrofit 接口定义
    │   │   └── response/      # 网络响应模型
    │   └── local/             # 本地数据源
    │       ├── db/            # Room 数据库相关
    │       └── preferences/   # SharedPreferences 管理
    │
    └── datasource/            # （可选）数据源接口模块
        ├── IHomeDataSource.java
        └── ... (定义接口，供业务组件依赖，实现解耦)
```

## 组件化架构优势

### 1. 职责清晰
- **业务组件**：专注于具体业务逻辑和UI实现
- **数据层**：统一管理数据获取、缓存和存储
- **基础组件**：提供通用功能和工具

### 2. 独立开发
- 各业务组件可以独立编译和运行
- 团队可以并行开发不同模块
- 降低模块间的耦合度

### 3. 易于维护
- 数据逻辑集中管理，便于统一维护
- 接口化通信，降低组件间依赖
- 清晰的模块边界，便于问题定位

### 4. 可扩展性
- 新增业务组件不影响现有模块
- 数据层支持多种数据源扩展
- 基础组件可复用于多个项目



## MVVM的价值体现

通过这个例子可以看出：

1. **Model层**：专注于数据的获取和存储
2. **ViewModel层**：负责数据转换和业务逻辑处理
3. **View层**：纯粹的UI渲染，根据状态展示不同界面

### 核心优势

- **职责分离**：每一层都有明确的职责
- **数据驱动**：UI完全由数据状态决定
- **易于测试**：业务逻辑集中在ViewModel，便于单元测试
- **状态管理**：统一的状态管理，避免UI状态混乱

## 总结

MVVM不仅仅是一种架构模式，更是一种**数据驱动思维**的体现。理解了"数据驱动UI"这个核心概念，你就掌握了现代前端开发的精髓。

记住：**好的架构不是为了炫技，而是为了让代码更清晰、更易维护、更易扩展。**