package com.mvvm.module_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Route(path = "/wallet/created")
class WalletCreatedSuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val walletName = intent.getStringExtra("wallet_name") ?: "我的钱包"
        val walletAddress = intent.getStringExtra("wallet_address") ?: ""
        
        setContent {
            MaterialTheme {
                WalletCreatedScreen(
                    walletName = walletName,
                    walletAddress = walletAddress,
                    onEnterWallet = {
                        // 跳转到钱包主页
                        startActivity(Intent(this, WalletMainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun WalletCreatedScreen(
    walletName: String = "我的钱包",
    walletAddress: String = "",
    onEnterWallet: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 成功图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎉",
                fontSize = 64.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 成功标题
        Text(
            text = "钱包创建成功！",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 成功描述
        Text(
            text = "恭喜您成功创建了数字钱包\n现在可以安全地管理您的数字资产",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 钱包信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "钱包信息",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 钱包名称
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "钱包名称:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = walletName,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 钱包地址
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "钱包地址:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = walletAddress,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 重要提醒
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔐 重要提醒",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "• 您的助记词已安全备份，请妥善保管\n• 助记词是恢复钱包的唯一方式\n• 请勿将助记词告诉任何人\n• 建议将助记词写在纸上并存放在安全的地方",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 进入钱包按钮
        Button(
            onClick = onEnterWallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "进入钱包",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 底部提示
        Text(
            text = "您可以随时在设置中查看钱包信息",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WalletCreatedScreenPreview() {
    MaterialTheme {
        WalletCreatedScreen(
            walletName = "我的钱包",
            walletAddress = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6"
        )
    }
}