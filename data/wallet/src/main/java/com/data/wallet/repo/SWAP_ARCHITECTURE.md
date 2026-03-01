# 兑换功能架构说明

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│                     Presentation Layer                   │
│                                                          │
│  ┌──────────────┐         ┌──────────────┐             │
│  │ SwapActivity │ ◄─────► │ SwapViewModel│             │
│  └──────────────┘         └──────┬───────┘             │
│                                   │                      │
└───────────────────────────────────┼──────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────┐
│                    Domain Layer   │                      │
│                                   │                      │
│  ┌────────────────────────────────▼──────────────┐      │
│  │         ISwapRepository (Interface)           │      │
│  │                                                │      │
│  │  • getTokenBalance()                          │      │
│  │  • getSwapQuote()                             │      │
│  │  • getAllowance()                             │      │
│  │  • approveToken()                             │      │
│  │  • executeSwap()                              │      │
│  │  • estimateSwapGas()                          │      │
│  │  • getTokenInfo()                             │      │
│  │  • getSupportedTokens()                       │      │
│  └───────────────────┬───────────────────────────┘      │
│                      │                                   │
└──────────────────────┼───────────────────────────────────┘
                       │
┌──────────────────────┼───────────────────────────────────┐
│                 Data Layer                               │
│                      │                                   │
│  ┌───────────────────▼──────────────────┐               │
│  │   SwapRepository (Implementation)    │               │
│  │                                       │               │
│  │  ┌─────────────────────────────────┐ │               │
│  │  │   Smart Contract Interaction    │ │               │
│  │  │                                 │ │               │
│  │  │  • ERC20 Contract               │ │               │
│  │  │  • Uniswap Router Contract      │ │               │
│  │  │  • Web3j Integration            │ │               │
│  │  └─────────────────────────────────┘ │               │
│  │                                       │               │
│  │  ┌─────────────────────────────────┐ │               │
│  │  │      Helper Components          │ │               │
│  │  │                                 │ │               │
│  │  │  • WalletStore                  │ │               │
│  │  │  • Credentials Management       │ │               │
│  │  │  • Path Builder                 │ │               │
│  │  └─────────────────────────────────┘ │               │
│  └───────────────────────────────────────┘               │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## 依赖注入

```
SwapDataModule (Hilt Module)
    │
    ├─► ISwapRepository ──► SwapRepository
    │
    └─► Singleton Scope
```

## 数据流

### 1. 查询余额流程
```
SwapViewModel
    │
    ├─► swapRepository.getTokenBalance()
    │       │
    │       ├─► Load ERC20 Contract
    │       ├─► Call balanceOf()
    │       └─► Format Balance
    │
    └─► Update UI State
```

### 2. 兑换流程
```
User Input
    │
    ├─► SwapViewModel.executeSwap()
    │       │
    │       ├─► Get Password from AccountRepository
    │       │
    │       ├─► swapRepository.executeSwap()
    │       │       │
    │       │       ├─► Load Wallet Credentials
    │       │       │
    │       │       ├─► Check if Token needs Approval
    │       │       │   │
    │       │       │   └─► If needed: approveToken()
    │       │       │
    │       │       ├─► Build Swap Path
    │       │       │
    │       │       ├─► Call Uniswap Router
    │       │       │   │
    │       │       │   ├─► ETH → Token: swapExactETHForTokens()
    │       │       │   ├─► Token → ETH: swapExactTokensForETH()
    │       │       │   └─► Token → Token: swapExactTokensForTokens()
    │       │       │
    │       │       └─► Return Transaction Hash
    │       │
    │       └─► Emit Success/Error Event
    │
    └─► Update UI (Show Result or Error)
```

### 3. 获取报价流程
```
Amount Input Change
    │
    ├─► SwapViewModel.updateFromAmount()
    │       │
    │       ├─► swapRepository.getSwapQuote()
    │       │       │
    │       │       ├─► Build Path
    │       │       ├─► Call Router.getAmountsOut()
    │       │       ├─► Calculate Price Impact
    │       │       └─► Calculate Minimum Received
    │       │
    │       └─► Update State with Quote
    │
    └─► UI Shows Exchange Rate & Details
```

## 关键组件说明

### ISwapRepository
- **职责**: 定义兑换相关的所有操作接口
- **位置**: `data/wallet/src/main/java/com/data/wallet/repo/ISwapRepository.kt`
- **特点**: 
  - 纯接口定义
  - 返回 Flow 支持响应式编程
  - 独立于具体实现

### SwapRepository
- **职责**: 实现 ISwapRepository 接口，与区块链交互
- **位置**: `data/wallet/src/main/java/com/data/wallet/repo/impl/SwapRepository.kt`
- **依赖**:
  - WalletStore: 获取钱包信息
  - Web3j: 与以太坊节点通信
- **核心功能**:
  - ERC20 合约调用
  - Uniswap Router 调用
  - 交易路径构建
  - Gas 估算

### SwapViewModel
- **职责**: 管理兑换页面的业务逻辑和状态
- **位置**: `feature/compose/src/main/java/com/mvvm/module_compose/SwapViewModel.kt`
- **依赖**:
  - ISwapRepository: 执行兑换操作
  - IWalletRepository: 获取钱包余额
  - IAccountRepository: 获取密码
- **状态管理**:
  - 使用 StateFlow 管理 UI 状态
  - 使用 SharedFlow 发送事件

### SwapActivity
- **职责**: 展示兑换 UI 和处理用户交互
- **位置**: `feature/compose/src/main/java/com/mvvm/module_compose/SwapActivity.kt`
- **特点**:
  - Jetpack Compose UI
  - 响应式数据绑定
  - 事件监听

## 扩展性设计

### 1. 支持多个 DEX
```kotlin
interface IDexAdapter {
    fun getQuote(...)
    fun executeSwap(...)
}

class UniswapAdapter : IDexAdapter { ... }
class SushiSwapAdapter : IDexAdapter { ... }
class PancakeSwapAdapter : IDexAdapter { ... }
```

### 2. 支持多链
```kotlin
interface IChainProvider {
    val chainId: Int
    val rpcUrl: String
    val dexRouter: String
}

class EthereumChain : IChainProvider { ... }
class BSCChain : IChainProvider { ... }
class PolygonChain : IChainProvider { ... }
```

### 3. 聚合器集成
```kotlin
interface IAggregator {
    fun getBestRoute(...)
    fun executeAggregatedSwap(...)
}

class OneInchAggregator : IAggregator { ... }
class ParaSwapAggregator : IAggregator { ... }
```

## 安全考虑

1. **私钥管理**: 通过 WalletStore 安全加载，不在内存中长期保存
2. **授权控制**: 使用精确授权而非无限授权
3. **滑点保护**: 强制设置最小接收金额
4. **交易验证**: 执行前验证所有参数
5. **错误处理**: 完善的异常捕获和用户提示

## 性能优化

1. **批量查询**: 使用 Multicall 减少 RPC 调用
2. **缓存策略**: 缓存代币信息和汇率
3. **异步处理**: 所有网络操作使用协程
4. **状态管理**: 使用 Flow 实现响应式更新

## 测试策略

1. **单元测试**: 测试 SwapRepository 的各个方法
2. **集成测试**: 测试完整的兑换流程
3. **UI 测试**: 测试用户交互和状态更新
4. **测试网测试**: 在 Goerli/Sepolia 上测试实际交易
