package com.mvvm.module_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.mvvm.module_compose.view.WalletEmptyScreen

/**
 * Created by yan_x
 * @date 2022/6/29/029 14:05
 * @description
 */
@Route(path = "/compose/main")
class ComPoseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // 检查用户是否有钱包，如果没有显示空状态页面
                        WalletEmptyScreen(
                            onCreateWallet = {
                                startActivity(Intent(this@ComPoseActivity, CreatePasswordActivity::class.java))
                                finish()
                            },
                            onImportWallet = {
                                startActivity(Intent(this@ComPoseActivity, WalletImportActivity::class.java))
                                finish()
                            }
                        )
                    }
                }

        }
    }


}
@Composable
@Preview
fun TitleView(){
    Column {
        Text("hello word", modifier = Modifier.padding(10.dp))
//        Text("hello word")
//        Text("hello word")
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    title: String,
//    navigationIconContent: @Composable () -> Unit,
//    scrollBehavior: TopAppBarScrollBehavior?,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        title = {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.icon_article_background),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(36.dp),
                )
                Text(
                    text = stringResource(R.string.published_in, title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
//        navigationIcon = navigationIconContent,
//        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}