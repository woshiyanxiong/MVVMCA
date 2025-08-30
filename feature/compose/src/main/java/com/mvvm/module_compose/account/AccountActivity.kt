package com.mvvm.module_compose.account

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route

@AndroidEntryPoint
@Route(path = "/wallet/account")
class AccountActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()
                AccountScreen(
                    state = state.value,
                    onBack = { finish() },
                    onCreateAccount = viewModel::createAccount,
                    onImportAccount = viewModel::importAccount,
                    onSelectAccount = viewModel::selectAccount,
                    onDeleteAccount = viewModel::deleteAccount
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountState = AccountState(),
    onBack: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onImportAccount: () -> Unit = {},
    onSelectAccount: (String) -> Unit = {},
    onDeleteAccount: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onCreateAccount) {
                        Icon(Icons.Default.Add, contentDescription = "创建账户")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(state.accounts) { account ->
                    AccountItem(
                        account = account,
                        isSelected = account.address == state.currentAccount,
                        onSelect = { onSelectAccount(account.address) },
                        onDelete = { onDeleteAccount(account.address) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    account: AccountInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatAddress(account.address),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${account.balance} ETH",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Row {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已选中",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatAddress(address: String): String {
    return if (address.length > 10) {
        "${address.substring(0, 6)}...${address.substring(address.length - 4)}"
    } else {
        address
    }
}