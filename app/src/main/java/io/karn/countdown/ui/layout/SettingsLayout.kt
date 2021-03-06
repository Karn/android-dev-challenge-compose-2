package io.karn.countdown.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import io.karn.countdown.MainViewModel

@Composable
fun SettingsLayout(
    navController: NavController,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val (useSystemSettings, darkMode) = viewModel.themeConfig.collectAsState(context = scope.coroutineContext).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = {},
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            contentColor = contentColorFor(MaterialTheme.colors.background),
            navigationIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "",
                    tint = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .clickable {
                            navController.navigateUp()
                        }
                        .padding(horizontal = 16.dp)
                )
            }
        )

        Text(
            text = "Dark mode",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Use system setting",
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            Switch(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                checked = useSystemSettings,
                onCheckedChange = { isChecked ->
                    viewModel.themeConfig.value = Pair(isChecked, darkMode)
                }
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Force enable dark mode",
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            Switch(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                enabled = !useSystemSettings,
                checked = darkMode,
                onCheckedChange = { isChecked ->
                    viewModel.themeConfig.value = Pair(useSystemSettings, isChecked)
                }
            )
        }

        Text(
            text = "Credits",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 4.dp)
        )

        Text(
            text = """
                """.trimIndent(),
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
    }
}