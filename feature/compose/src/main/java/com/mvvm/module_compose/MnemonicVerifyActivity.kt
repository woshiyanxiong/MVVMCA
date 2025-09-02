package com.mvvm.module_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/wallet/verify")
class MnemonicVerifyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val walletName = intent.getStringExtra("wallet_name") ?: "我的钱包"
        val mnemonic = intent.getStringArrayListExtra("mnemonic") ?: emptyList<String>()
        val walletAddress = intent.getStringExtra("wallet_address") ?: ""
        
        setContent {
            MaterialTheme {
                MnemonicVerifyScreen(
                    originalMnemonic = mnemonic,
                    onBackClick = { finish() },
                    onVerifySuccess = {
                        // 跳转到创建成功页面
                        startActivity(Intent(this, WalletCreatedSuccessActivity::class.java).apply {
                            putExtra("wallet_name", walletName)
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
fun MnemonicVerifyScreen(
    originalMnemonic: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onVerifySuccess: () -> Unit = {}
) {
    var selectedWords by remember { mutableStateOf(listOf<String>()) }
    var shuffledWords by remember { mutableStateOf(originalMnemonic.shuffled()) }
    
    val isComplete = selectedWords.size == originalMnemonic.size
    val isCorrect = selectedWords == originalMnemonic

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("验证助记词") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 标题和说明
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "验证助记词",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "请按正确顺序点击助记词",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 已选择的助记词显示区域
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
                        text = "已选择的助记词",
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(12) { index ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (index < selectedWords.size) {
                                            // 移除选中的单词
                                            val removedWord = selectedWords[index]
                                            selectedWords = selectedWords.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            shuffledWords = shuffledWords + removedWord
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (index < selectedWords.size) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
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
                                        color = if (index < selectedWords.size) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (index < selectedWords.size) selectedWords[index] else "",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (index < selectedWords.size) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 可选择的助记词
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "请选择助记词",
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(160.dp)
                    ) {
                        itemsIndexed(shuffledWords) { _, word ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedWords.size < 12) {
                                            selectedWords = selectedWords + word
                                            shuffledWords = shuffledWords - word
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = word,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 验证结果提示
            if (isComplete) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isCorrect) "✓ 验证成功！" else "✗ 顺序错误，请重新排列",
                        modifier = Modifier.padding(16.dp),
                        color = if (isCorrect) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedWords = emptyList()
                        shuffledWords = originalMnemonic.shuffled()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("重置")
                }
                
                Button(
                    onClick = onVerifySuccess,
                    modifier = Modifier.weight(1f),
                    enabled = isComplete && isCorrect,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("完成")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MnemonicVerifyScreenPreview() {
    MaterialTheme {
        MnemonicVerifyScreen(
            originalMnemonic = listOf(
                "abandon", "ability", "able", "about", "above", "absent",
                "absorb", "abstract", "absurd", "abuse", "access", "accident"
            )
        )
    }
}