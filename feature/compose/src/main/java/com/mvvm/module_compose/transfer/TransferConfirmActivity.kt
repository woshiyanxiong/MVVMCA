package com.mvvm.module_compose.transfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Route(path = "/wallet/transfer/confirm")
class TransferConfirmActivity : ComponentActivity() {
    private val viewModel: TransferConfirmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val toAddress = intent.getStringExtra("toAddress") ?: ""
        val amount = intent.getStringExtra("amount") ?: ""
        val balance = intent.getStringExtra("balance") ?: "0.0"
        viewModel.init(toAddress, amount, balance)

        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(state.value.isSuccess) {
                    if (state.value.isSuccess) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }

                TransferConfirmScreen(
                    state = state.value,
                    onConfirm = { viewModel.showPasswordDialog() },
                    onDismiss = { finish() },
                    onPasswordConfirm = { password -> viewModel.executeTransfer(password) },
                    onPasswordDismiss = { viewModel.hidePasswordDialog() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferConfirmScreen(
    state: TransferConfirmState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onPasswordConfirm: (String) -> Unit,
    onPasswordDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 发送图标
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text("⬆", fontSize = 36.sp, color = Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("发送", fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(24.dp))

            // 金额
            Text(
                text = "-${state.amount} ETH",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.fiatValue,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 详情卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 网络费用
                    DetailRow(
                        label = "Ethereum 网络费用",
                        value = state.networkFee,
                        hasChevron = true
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 发送地址
                    AddressRow(label = "发送地址", address = state.fromAddress)

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 收款地址
                    AddressRow(label = "收款地址", address = state.toAddress)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("拒绝", fontSize = 16.sp)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("确认", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }

    // 密码弹框
    if (state.showPasswordDialog) {
        PasswordDialog(
            onDismiss = onPasswordDismiss,
            onConfirm = onPasswordConfirm,
            isLoading = state.isLoading,
            error = state.passwordError
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, hasChevron: Boolean = false) {
    Column {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 16.sp, modifier = Modifier.weight(1f))
            if (hasChevron) {
                Text(">", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddressRow(label: String, address: String) {
    Column {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = address,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = { /* TODO: copy */ }, modifier = Modifier.size(24.dp)) {
                Text(
                    "📋",
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun TransferConfirmScreenPreview() {
    MaterialTheme {
        TransferConfirmScreen(
            state = TransferConfirmState(
                toAddress = "0xa6258ac81fe74e8963b6cc11252f5079f64120d3",
                fromAddress = "0x15cf5b56ec33200fbc0afcf6125e9425e9dd5aa7",
                amount = "0.0001",
                fiatValue = "$0.19",
                networkFee = "0.000001 ETH (< $0.01)"
            ),
            onConfirm = {},
            onDismiss = {},
            onPasswordConfirm = {},
            onPasswordDismiss = {}
        )
    }
}
