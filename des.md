# 什么是MVVM

基本上很多开发人员都被问过MVC、MVP、MVVM之间的区别吧。不知道回答这一堆专业术语的背后是否想过它们到底为什么服务？

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
)
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
            0 -> InfoUIState(isReviewing = true)
            1 -> InfoUIState(isRejected = true, showRetryButton = true)
            2 -> InfoUIState(isApproved = true)
            else -> InfoUIState()
        }
    }
}
```

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