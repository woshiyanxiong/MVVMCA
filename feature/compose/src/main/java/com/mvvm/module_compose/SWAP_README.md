# 代币兑换功能说明

## 功能概述

实现了一个 ETH ⇄ USDT 的代币兑换功能页面，支持双向兑换。采用独立的 SwapRepository 架构，便于后续扩展。

## 架构设计

### 仓库分层
```
ISwapRepository (接口)
    ↓
SwapRepository (实现)
    ↓
SwapViewModel
    ↓
SwapActivity (UI)
```

### 职责划分
- **IWalletRepository**: 钱包基础操作（余额查询、转账等）
- **ISwapRepository**: 代币兑换专用（DEX 交互、ERC20 操作等）
- **IAccountRepository**: 账户管理（密码、账户信息等）

## 已实现功能

### UI 层面
- ✅ 兑换页面布局（SwapActivity）
- ✅ 代币输入卡片组件
- ✅ 代币交换方向切换
- ✅ 实时汇率显示
- ✅ 价格影响提示
- ✅ 最少接收金额（滑点保护）
- ✅ 网络费用预估
- ✅ 余额显示
- ✅ 输入验证和错误提示

### 业务逻辑
- ✅ 金额输入和验证
- ✅ 余额检查
- ✅ 汇率计算（模拟）
- ✅ 滑点保护计算（0.5%）
- ✅ 代币方向交换
- ✅ 密码从本地读取
- ✅ 独立的 SwapRepository 架构
- ✅ Hilt 依赖注入配置

### 数据层
- ✅ ISwapRepository 接口定义
- ✅ SwapRepository 实现框架
- ✅ SwapDataModule 依赖注入模块
- ✅ 数据模型（TokenInfo, TokenBalance, SwapQuote）

## 待实现功能（TODO）

### 1. ISwapRepository 接口实现

所有待实现的方法都在 `SwapRepository.kt` 中标记了 TODO，主要包括：

#### 1.1 ERC20 代币余额查询
```kotlin
override fun getTokenBalance(tokenAddress: String, walletAddress: String): Flow<TokenBalance?>
```

实现要点：
- 使用 Web3j 调用 ERC20 合约的 `balanceOf` 方法
- USDT 合约地址：`0xdac17f958d2ee523a2206206994597c13d831ec7`
- 需要处理不同代币的 decimals
- 格式化余额显示

#### 1.2 ERC20 代币授权检查
```kotlin
override fun getAllowance(
    tokenAddress: String,
    ownerAddress: String,
    spenderAddress: String
): Flow<BigInteger?>
```

实现要点：
- 调用 ERC20 合约的 `allowance` 方法
- 检查当前授权额度是否足够
- 用于判断是否需要先执行 approve

#### 1.3 ERC20 代币授权
```kotlin
override fun approveToken(
    tokenAddress: String,
    spenderAddress: String,
    amount: String,
    password: String
): Flow<String?>
```

实现要点：
- 调用 ERC20 合约的 `approve` 方法
- 授权给 DEX 路由合约（如 Uniswap Router）
- 建议使用精确授权而非无限授权
- 返回交易哈希

#### 1.4 DEX 兑换执行
```kotlin
override fun executeSwap(
    fromToken: String,
    toToken: String,
    fromAmount: String,
    minToAmount: String,
    password: String,
    deadline: Long?
): Flow<String?>
```

实现要点：
- 如果是 ETH -> Token：调用 `swapExactETHForTokens`
- 如果是 Token -> ETH：先检查授权，再调用 `swapExactTokensForETH`
- 如果是 Token -> Token：先检查授权，再调用 `swapExactTokensForTokens`
- 需要设置 deadline（交易过期时间）
- 需要构建正确的 path（交易路径）

#### 1.5 获取兑换报价
```kotlin
override fun getSwapQuote(fromToken: String, toToken: String, amount: String): Flow<SwapQuote?>
```

实现要点：
- 调用 Uniswap Router 的 `getAmountsOut` 方法
- 构建正确的交易路径
- 计算价格影响
- 计算滑点保护后的最少接收金额

### 2. 代币信息管理

#### 2.1 获取代币信息
```kotlin
override fun getTokenInfo(tokenAddress: String): Flow<TokenInfo?>
```

实现要点：
- 调用 ERC20 合约的 `name()`, `symbol()`, `decimals()`
- 缓存常用代币信息
- 支持自定义代币添加

#### 2.2 批量查询余额
```kotlin
override fun getTokenBalances(tokenAddresses: List<String>, walletAddress: String): Flow<List<TokenBalance>>
```

实现要点：
- 使用 Multicall 合约优化性能
- 减少 RPC 调用次数

### 3. Gas 费用估算

```kotlin
override fun estimateSwapGas(fromToken: String, toToken: String, amount: String): Flow<String?>
```

实现要点：
- 使用 `eth_estimateGas` RPC 方法
- 考虑当前网络拥堵情况
- 提供快速/标准/慢速选项

### 4. DEX 集成

推荐使用 Uniswap V2/V3 或 SushiSwap：

#### Uniswap V2 Router 地址
- 主网：`0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D`

#### 关键方法
```solidity
// ETH -> Token
function swapExactETHForTokens(
    uint amountOutMin,
    address[] calldata path,
    address to,
    uint deadline
) external payable returns (uint[] memory amounts);

// Token -> ETH
function swapExactTokensForETH(
    uint amountIn,
    uint amountOutMin,
    address[] calldata path,
    address to,
    uint deadline
) external returns (uint[] memory amounts);
```

### 3. 实时汇率获取

#### 3.1 从 DEX 获取报价
```kotlin
override fun getSwapQuote(fromToken: String, toToken: String, amount: String): Flow<SwapQuote?>
```

实现方式：
- 调用 Uniswap Router 的 `getAmountsOut` 方法
- 或使用 Chainlink 价格预言机
- 或调用 CoinGecko/CoinMarketCap API

#### 3.2 价格影响计算
```kotlin
// 计算价格影响百分比
fun calculatePriceImpact(
    inputAmount: BigDecimal,
    outputAmount: BigDecimal,
    marketPrice: BigDecimal
): String
```

### 4. Gas 费用估算

```kotlin
override fun estimateSwapGas(fromToken: String, toToken: String, amount: String): Flow<String?>
```

实现要点：
- 使用 `eth_estimateGas` RPC 方法
- 考虑当前网络拥堵情况
- 提供快速/标准/慢速选项

### 5. 交易状态追踪

```kotlin
// 监听交易状态
fun watchTransaction(txHash: String): Flow<TransactionStatus>

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}
```

### 6. 高级功能

#### 6.1 代币选择器
- 支持搜索代币
- 显示常用代币列表（已在 `getSupportedTokens()` 中定义）
- 支持自定义添加代币

#### 6.2 交易设置
- 滑点容忍度设置（0.1%, 0.5%, 1%, 自定义）
- 交易截止时间设置
- Gas 价格设置

#### 6.3 交易历史
- 显示兑换历史记录
- 交易详情查看
- 在区块链浏览器中查看

## 技术实现建议

### 1. Web3j 合约调用示例

```kotlin
// 加载 ERC20 合约
val contract = ERC20.load(
    tokenAddress,
    web3j,
    credentials,
    DefaultGasProvider()
)

// 查询余额
val balance = contract.balanceOf(walletAddress).send()

// 授权
val approveTx = contract.approve(
    spenderAddress,
    amount
).send()
```

### 2. Uniswap 路由调用示例

```kotlin
// 构建交易路径
val path = listOf(
    "0x0000000000000000000000000000000000000000", // WETH
    "0xdac17f958d2ee523a2206206994597c13d831ec7"  // USDT
)

// 调用 swap 方法
val swapTx = routerContract.swapExactETHForTokens(
    minAmountOut,
    path,
    toAddress,
    deadline
).send()
```

### 3. 错误处理

需要处理的常见错误：
- 余额不足
- 授权额度不足
- 滑点过大
- 交易超时
- Gas 不足
- 网络错误

## 安全注意事项

1. **滑点保护**：始终设置合理的最小接收金额
2. **授权管理**：不要授权无限额度，使用精确授权
3. **交易确认**：显示详细的交易信息供用户确认
4. **私钥安全**：确保密码和私钥的安全存储
5. **合约验证**：使用经过审计的 DEX 合约

## 测试建议

1. 在测试网（Goerli/Sepolia）上测试
2. 使用小额测试交易
3. 测试各种边界情况（余额不足、滑点过大等）
4. 测试网络异常情况

## 路由配置

在 AndroidManifest.xml 中添加：
```xml
<activity
    android:name=".SwapActivity"
    android:exported="true" />
```

## 使用方式

```kotlin
// 通过 ARouter 跳转
ARouter.getInstance()
    .build("/wallet/swap")
    .navigation()
```

## 参考资源

- [Uniswap V2 文档](https://docs.uniswap.org/protocol/V2/introduction)
- [Web3j 文档](https://docs.web3j.io/)
- [ERC20 标准](https://eips.ethereum.org/EIPS/eip-20)
- [Chainlink 价格预言机](https://docs.chain.link/data-feeds/price-feeds)
