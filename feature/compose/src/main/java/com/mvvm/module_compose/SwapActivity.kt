package com.mvvm.module_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import kotlinx.coroutines.launch

@AndroidEntryPoint
@Route(path = "/wallet/swap")
class SwapActivity : ComponentActivity() {
    private val viewModel: SwapViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 监听兑换成功事件
        lifecycleScope.launch {
            viewModel.swapSuccessEvent.collect {
                // TODO: 显示成功提示
                finish()
            }
        }
        
        // 监听兑换错误事件
        lifecycleScope.launch {
            viewModel.swapErrorEvent.collect { errorMsg ->
                // TODO: 显示错误提示
            }
        }
        
        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()
                SwapScreen(
                    state = state.value,
                    onFromAmountChange = viewModel::updateFromAmount,
                    onSwapTokens = viewModel::swapTokens,
                    onSwap = viewModel::executeSwap,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapScreen(
    state: SwapState = SwapState(),
    onFromAmountChange: (String) -> Unit = {},
    onSwapTokens: () -> Unit = {},
    onSwap: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "兑换",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 打开设置 */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 从代币卡片
            TokenInputCard(
                label = "支付",
                token = state.fromToken,
                amount = state.fromAmount,
                balance = state.fromBalance,
                onAmountChange = onFromAmountChange,
                isError = state.fromAmountError != null
            )
            
            if (state.fromAmountError != null) {
                Text(
                    text = state.fromAmountError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // 交换按钮
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSwapTokens,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "交换",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 到代币卡片
            TokenInputCard(
                label = "接收",
                token = state.toToken,
                amount = state.toAmount,
                balance = state.toBalance,
                onAmountChange = {},
                isError = false,
                readOnly = true
            )
            
            // 兑换信息卡片
            if (state.fromAmount.isNotEmpty() && state.fromAmount.toDoubleOrNull() != null && state.fromAmount.toDouble() > 0) {
                SwapInfoCard(
                    exchangeRate = state.exchangeRate,
                    priceImpact = state.priceImpact,
                    minimumReceived = state.minimumReceived,
                    networkFee = state.networkFee,
                    fromToken = state.fromToken,
                    toToken = state.toToken
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 兑换按钮
            Button(
                onClick = onSwap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = state.isValid && !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "兑换中...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.fromAmount.isEmpty()) "输入金额" else "确认兑换",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun TokenInputCard(
    label: String,
    token: TokenInfo,
    amount: String,
    balance: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean,
    readOnly: Boolean = false
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "余额: $balance ${token.symbol}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 代币选择器
                Surface(
                    modifier = Modifier.clickable { /* TODO: 打开代币选择器 */ },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = token.symbol.first().toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = token.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0.0", fontSize = 24.sp) },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(180.dp),
                    singleLine = true,
                    isError = isError,
                    readOnly = readOnly,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun SwapInfoCard(
    exchangeRate: String,
    priceImpact: String,
    minimumReceived: String,
    networkFee: String,
    fromToken: TokenInfo,
    toToken: TokenInfo
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SwapInfoRow("汇率", "1 ${fromToken.symbol} ≈ $exchangeRate ${toToken.symbol}")
            SwapInfoRow("价格影响", priceImpact)
            SwapInfoRow("最少接收", "$minimumReceived ${toToken.symbol}")
            SwapInfoRow("网络费用", "$networkFee ETH")
        }
    }
}

@Composable
fun SwapInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 代币信息
 */
data class TokenInfo(
    val symbol: String,
    val name: String,
    val address: String,
    val decimals: Int = 18
)

/**
 * 兑换页面状态
 */
data class SwapState(
    val fromToken: TokenInfo = TokenInfo("ETH", "Ethereum", com.data.wallet.util.WeiConverter.ETH_ADDRESS),
    val toToken: TokenInfo = TokenInfo("USDT", "Tether USD", "0xdac17f958d2ee523a2206206994597c13d831ec7"),
    val fromAmount: String = "",
    val toAmount: String = "",
    val fromBalance: String = "0.0",
    val toBalance: String = "0.0",
    val exchangeRate: String = "0.0",
    val priceImpact: String = "< 0.01%",
    val minimumReceived: String = "0.0",
    val networkFee: String = "0.0",
    val fromAmountError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isValid: Boolean = false
)
