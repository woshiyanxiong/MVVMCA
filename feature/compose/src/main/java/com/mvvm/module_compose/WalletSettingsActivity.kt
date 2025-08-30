package com.mvvm.module_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.mvvm.module_compose.vm.WalletSettingsState
import com.mvvm.module_compose.vm.WalletSettingsViewModel

@AndroidEntryPoint
@Route(path = "/wallet/settings")
class WalletSettingsActivity : ComponentActivity() {
    private val viewModel: WalletSettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()
                WalletSettingsScreen(
                    state = state.value,
                    onBack = { finish() },
                    onBackupMnemonic = viewModel::backupMnemonic,
                    onChangePassword = viewModel::changePassword,
                    onDeleteWallet = viewModel::deleteWallet
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsState = WalletSettingsState(),
    onBack: () -> Unit = {},
    onBackupMnemonic: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onDeleteWallet: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 安全设置
            item {
                SettingsSection(
                    title = "安全设置",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Create,
                            title = "备份助记词",
                            subtitle = "备份您的助记词以确保资产安全",
                            onClick = onBackupMnemonic
                        ),
                        SettingsItem(
                            icon = Icons.Default.Lock,
                            title = "修改密码",
                            subtitle = "更改钱包密码",
                            onClick = onChangePassword
                        )
                    )
                )
            }
            
            // 钱包管理
            item {
                SettingsSection(
                    title = "钱包管理",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.AccountBox,
                            title = "钱包信息",
                            subtitle = state.walletAddress,
                            onClick = {}
                        ),
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = "删除钱包",
                            subtitle = "永久删除此钱包",
                            onClick = onDeleteWallet,
                            isDestructive = true
                        )
                    )
                )
            }
            
            // 通用设置
            item {
                SettingsSection(
                    title = "通用设置",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.ThumbUp,
                            title = "语言",
                            subtitle = "中文简体",
                            onClick = {}
                        ),
                        SettingsItem(
                            icon = Icons.Default.Settings,
                            title = "主题",
                            subtitle = "跟随系统",
                            onClick = {}
                        )
                    )
                )
            }
            
            // 关于
            item {
                SettingsSection(
                    title = "关于",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "版本信息",
                            subtitle = "v1.0.0",
                            onClick = {}
                        ),
                        SettingsItem(
                            icon = Icons.Default.AccountCircle,
                            title = "用户协议",
                            subtitle = "查看用户协议和隐私政策",
                            onClick = {}
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            items.forEachIndexed { index, item ->
                SettingsItemRow(item = item)
                if (index < items.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (item.isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)