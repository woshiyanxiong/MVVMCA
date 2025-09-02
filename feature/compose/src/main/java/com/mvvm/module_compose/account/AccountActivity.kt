package com.mvvm.module_compose.account

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.mvvm.module_compose.WalletMainActivity

@AndroidEntryPoint
@Route(path = "/wallet/login")
class AccountActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 监听登录成功事件
        lifecycleScope.launch {
            viewModel.loginSuccessEvent.collect {
                startActivity(Intent(this@AccountActivity, WalletMainActivity::class.java))
                finish()
            }
        }
        
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                WalletLoginScreen(
                    state = state,
                    onPasswordChange = viewModel::updatePassword,
                    onLogin = viewModel::login,
                    onCreateWallet = { /* 跳转到创建钱包页面 */ },
                    onImportWallet = { /* 跳转到导入钱包页面 */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun WalletLoginScreen(
    state: AccountState = AccountState(),
    onPasswordChange: (String) -> Unit = {},
    onLogin: () -> Unit = {},
    onCreateWallet: () -> Unit = {},
    onImportWallet: () -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo区域
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦊",
                    fontSize = 32.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "欢迎回来！",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "解锁您的钱包",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                onPasswordChange(it)
            },
            label = { Text("密码") },
            placeholder = { Text("请输入钱包密码") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.error != null
        )
        
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 解锁按钮
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = password.isNotEmpty() && !state.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "解锁",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 底部选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onImportWallet) {
                Text("导入钱包")
            }
            
            TextButton(onClick = onCreateWallet) {
                Text("创建钱包")
            }
        }
    }
}

