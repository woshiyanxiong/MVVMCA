package com.mvvm.module_compose.swap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.alibaba.android.arouter.facade.annotation.Route
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Route(path = "/wallet/swaptoken")
class SwapTokenActivity : ComponentActivity() {
    private val viewModel: SwapTokenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val state = viewModel.state.collectAsStateWithLifecycle()
                SwapTokenScreen(
                    state = state.value,
                    onFromAmountChange = viewModel::updateFromAmount,
                    onSwapTokens = viewModel::swapTokens,
                    onSwap = viewModel::executeSwap,
                    onFromTokenClick = viewModel::showFromTokenPicker,
                    onToTokenClick = viewModel::showToTokenPicker,
                    onDismissTokenPicker = viewModel::dismissTokenPicker,
                    onSelectFromToken = viewModel::selectFromToken,
                    onSelectToToken = viewModel::selectToToken,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapTokenScreen(
    state: SwapTokenState,
    onFromAmountChange: (String) -> Unit,
    onSwapTokens: () -> Unit,
    onSwap: () -> Unit,
    onFromTokenClick: () -> Unit,
    onToTokenClick: () -> Unit,
    onDismissTokenPicker: () -> Unit,
    onSelectFromToken: (SwapTokenInfo) -> Unit,
    onSelectToToken: (SwapTokenInfo) -> Unit,
    onBack: () -> Unit
) {
    // 币种选择弹框
    if (state.showFromTokenPicker) {
        TokenPickerDialog(
            title = "选择支付币种",
            tokens = state.tokenList,
            selectedSymbol = state.fromToken.symbol,
            excludeSymbol = state.toToken.symbol,
            onSelect = onSelectFromToken,
            onDismiss = onDismissTokenPicker
        )
    }
    if (state.showToTokenPicker) {
        TokenPickerDialog(
            title = "选择接收币种",
            tokens = state.tokenList,
            selectedSymbol = state.toToken.symbol,
            excludeSymbol = state.fromToken.symbol,
            onSelect = onSelectToToken,
            onDismiss = onDismissTokenPicker
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("兑换", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            // 支付卡片
            SwapInputCard(
                label = "支付",
                token = state.fromToken,
                amount = state.fromAmount,
                balance = state.fromBalance,
                onAmountChange = onFromAmountChange,
                onTokenClick = onFromTokenClick,
                isError = state.fromAmountError != null,
                readOnly = false
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
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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

            // 接收卡片
            SwapInputCard(
                label = "接收",
                token = state.toToken,
                amount = state.toAmount,
                balance = "",
                onAmountChange = {},
                onTokenClick = onToTokenClick,
                isError = false,
                readOnly = true
            )

            // 兑换信息
            if (state.fromAmount.isNotEmpty() && state.fromAmount.toDoubleOrNull() != null && state.fromAmount.toDouble() > 0) {
                SwapDetailCard(state)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 兑换按钮
            Button(
                onClick = onSwap,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.isValid && !state.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("兑换中...", fontSize = 16.sp)
                } else {
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

/**
 * 代币输入卡片
 */
@Composable
fun SwapInputCard(
    label: String,
    token: SwapTokenInfo,
    amount: String,
    balance: String,
    onAmountChange: (String) -> Unit,
    onTokenClick: () -> Unit,
    isError: Boolean,
    readOnly: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (balance.isNotEmpty()) {
                    Text(
                        text = "余额: $balance ${token.symbol}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 代币选择器（可点击）
                Surface(
                    modifier = Modifier.clickable(onClick = onTokenClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 代币图标
                        if (token.logoUrl != null) {
                            AsyncImage(
                                model = token.logoUrl,
                                contentDescription = token.symbol,
                                modifier = Modifier.size(28.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = token.symbol.first().toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = token.symbol, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
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

/**
 * 兑换详情卡片
 */
@Composable
fun SwapDetailCard(state: SwapTokenState) {
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
            DetailRow("汇率", "1 ${state.fromToken.symbol} ≈ ${state.exchangeRate} ${state.toToken.symbol}")
            DetailRow("价格影响", state.priceImpact)
            DetailRow("最少接收", "${state.minimumReceived} ${state.toToken.symbol}")
            DetailRow("网络费用", "${state.networkFee} ETH")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * 币种选择弹框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenPickerDialog(
    title: String,
    tokens: List<SwapTokenInfo>,
    selectedSymbol: String,
    excludeSymbol: String,
    onSelect: (SwapTokenInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTokens = tokens.filter { token ->
        token.symbol != excludeSymbol &&
        (searchQuery.isEmpty() ||
            token.symbol.contains(searchQuery, ignoreCase = true) ||
            token.name.contains(searchQuery, ignoreCase = true))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索币种名称或符号") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredTokens) { token ->
                    val isSelected = token.symbol == selectedSymbol
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(token) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                               else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 代币图标
                            if (token.logoUrl != null) {
                                AsyncImage(
                                    model = token.logoUrl,
                                    contentDescription = token.symbol,
                                    modifier = Modifier.size(36.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = token.symbol.first().toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = token.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = token.name,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选择",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
