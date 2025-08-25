package com.mvvm.module_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import androidx.compose.foundation.layout.size

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
                    onLoadData = { viewModel.loadWalletData() }
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
    val icon: String = "💰"
)

data class Transaction(
    val type: String, // "send" or "receive"
    val amount: String,
    val symbol: String,
    val address: String,
    val time: String,
    val status: String = "success"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletMainScreen(
    state: WalletMainState = WalletMainState(),
    onLoadData: () -> Unit = {}
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
                    IconButton(onClick = { }) {
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
                                text = state.ethValue,
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
                        icon = Icons.Default.Send,
                        text = "转账",
                        onClick = { }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        text = "收款",
                        onClick = { }
                    )
                    QuickActionButton(
                        icon = Icons.Default.List,
                        text = "记录",
                        onClick = { }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Refresh,
                        text = "兑换",
                        onClick = { }
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
                        } else {
                            Text(
                                text = "暂无交易记录",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AssetItem(asset: Asset) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = asset.icon,
                fontSize = 24.sp,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .wrapContentSize()
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = asset.name,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = asset.symbol,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = asset.balance,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = asset.value,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (transaction.type == "receive") 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "receive") 
                        Icons.Default.KeyboardArrowDown 
                    else 
                        Icons.Default.KeyboardArrowUp,
                    contentDescription = transaction.type,
                    tint = if (transaction.type == "receive") 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (transaction.type == "receive") "收到" else "发送",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${if (transaction.type == "receive") "+" else "-"}${transaction.amount} ${transaction.symbol}",
                fontWeight = FontWeight.Medium,
                color = if (transaction.type == "receive") 
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

@Preview(showBackground = true)
@Composable
fun WalletMainScreenPreview() {
    MaterialTheme {
        WalletMainScreen()
    }
}