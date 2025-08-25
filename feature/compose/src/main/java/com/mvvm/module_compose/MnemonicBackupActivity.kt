package com.mvvm.module_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/wallet/backup")
class MnemonicBackupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val walletName = intent.getStringExtra("wallet_name") ?: "我的钱包"
        val mnemonic = intent.getStringArrayListExtra("mnemonic") ?: emptyList<String>()
        val walletAddress = intent.getStringExtra("wallet_address") ?: ""
        
        setContent {
            MaterialTheme {
                MnemonicBackupScreen(
                    walletName = walletName,
                    mnemonic = mnemonic,
                    walletAddress = walletAddress,
                    onBackClick = { finish() },
                    onContinue = {
                        // 跳转到助记词验证页面
                        startActivity(Intent(this, MnemonicVerifyActivity::class.java).apply {
                            putExtra("wallet_name", walletName)
                            putStringArrayListExtra("mnemonic", ArrayList(mnemonic))
                            putExtra("wallet_address", walletAddress)
                        })
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MnemonicBackupScreen(
    walletName: String = "我的钱包",
    mnemonic: List<String> = emptyList(),
    walletAddress: String = "",
    onBackClick: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    var isRevealed by remember { mutableStateOf(false) }
    
    val mnemonicWords = mnemonic.ifEmpty {
        listOf(
            "abandon", "ability", "able", "about", "above", "absent",
            "absorb", "abstract", "absurd", "abuse", "access", "accident"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备份助记词") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 标题和说明
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "备份您的助记词",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "助记词是恢复钱包的唯一方式，请务必安全保存",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 安全警告卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ 重要提醒",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "• 请在安全的环境下查看助记词\n• 不要截屏或拍照\n• 不要在网络上分享\n• 妥善保管，丢失后无法找回",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 20.sp
                    )
                }
            }

            // 助记词显示区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "您的助记词",
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (isRevealed) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            itemsIndexed(mnemonicWords) { index, word ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = word,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "👁️",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = "点击下方按钮显示助记词",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (!isRevealed) {
                        Button(
                            onClick = { isRevealed = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("显示助记词")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 继续按钮
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isRevealed,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "我已安全备份",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MnemonicBackupScreenPreview() {
    MaterialTheme {
        MnemonicBackupScreen()
    }
}