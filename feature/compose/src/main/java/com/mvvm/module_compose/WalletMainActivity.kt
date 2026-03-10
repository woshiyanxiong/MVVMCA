package com.mvvm.module_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.text.style.TextOverflow
import com.mvvm.module_compose.uistate.TransactionUIState
import com.mvvm.module_compose.vm.WalletMainState
import com.mvvm.module_compose.vm.WalletMainViewModel

@AndroidEntryPoint
@Route(path = "/wallet/main")
class WalletMainActivity : ComponentActivity() {
    private val viewModel: WalletMainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()
                WalletMainScreen(
                    state = state.value,
                    onLoadData = { viewModel.loadWalletData() },
                    context = this@WalletMainActivity
                )
            }
        }
    }
}

data class Asset(
    val name: String,
    val symbol: String,
    val balance: String,
    val value: String,
    val icon: String = "💰",
    val logoUrl: String? = null
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletMainScreen(
    state: WalletMainState = WalletMainState(),
    onLoadData: () -> Unit = {},
    context: android.content.Context = androidx.compose.ui.platform.LocalContext.current
) {
    // 页面加载时获取数据
    LaunchedEffect(Unit) {
        onLoadData()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("我的钱包", fontSize = 18.sp)
                        Text(
                            text = state.walletAddress,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        WalletNavigator.navigateToSettings(context)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 余额卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "总资产",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = state.totalValue,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.ethBalance} ETH",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            // 快捷操作
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        iconRes = R.drawable.ic_send,
                        text = "转账",
                        onClick = { WalletNavigator.navigateToTransfer(context) }
                    )
                    QuickActionButton(
                        iconRes = R.drawable.ic_receive,
                        text = "收款",
                        onClick = { WalletNavigator.navigateToReceive(context) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.List,
                        text = "记录",
                        onClick = { }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Refresh,
                        text = "兑换",
                        onClick = { WalletNavigator.navigateToSwap(context) }
                    )
                }
            }
            
            // 资产列表
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "我的资产",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // ETH 资产
                        AssetItem(
                            asset = Asset(
                                name = "Ethereum",
                                symbol = "ETH",
                                balance = state.ethBalance,
                                value = state.ethValue,
                                icon = "⟠"
                            )
                        )
                        
                        // ERC20 代币列表
                        state.tokenBalances.forEach { token ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            AssetItem(
                                asset = Asset(
                                    name = token.name,
                                    symbol = token.symbol,
                                    balance = token.balance,
                                    value = "",
                                    logoUrl = token.logo
                                )
                            )
                        }
                    }
                }
            }
            
            // 最近交易
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "最近交易",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { }) {
                                Text("查看全部")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (state.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (state.transactions.isEmpty()) {
                            Text(
                                text = "暂无交易记录",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        } else {
                            state.transactions.forEach { transaction ->
                                TransactionItem(
                                    transaction = transaction
                                )
                                if (transaction != state.transactions.last()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionItemView(){
    TransactionItem(TransactionUIState(
        isReceive = true,
        amount = "0.01",
        symbol = "ETH",
        address = "0x1234567890",
        time = "2023-01-01 12:00:00"
    ))
}


@Composable
fun TransactionItem(transaction: TransactionUIState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (transaction.isReceive)
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isReceive)
                        Icons.Default.KeyboardArrowDown 
                    else 
                        Icons.Default.KeyboardArrowUp,
                    contentDescription = "${transaction.isReceive}",
                    tint = if (transaction.isReceive)
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                Text(
                    text = if (transaction.isReceive) "收到" else "发送",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = transaction.address,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    softWrap = false,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${if (transaction.isReceive) "+" else "-"}${transaction.amount} ${transaction.symbol}",
                fontWeight = FontWeight.Medium,
                color = if (transaction.isReceive)
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            Text(
                text = transaction.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 辅助函数
fun convertWeiToEth(wei: String): String {
    return com.data.wallet.util.WeiConverter.weiHexToEthString(wei)
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}

@Preview(showBackground = true)
@Composable
fun WalletMainScreenPreview() {
    MaterialTheme {
        WalletMainScreen()
    }
}