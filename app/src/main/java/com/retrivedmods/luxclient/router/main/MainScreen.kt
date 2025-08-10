package com.retrivedmods.luxclient.router.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Help
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrivedmods.luxclient.R
import com.retrivedmods.luxclient.ui.component.NavigationRailX
import com.retrivedmods.luxclient.viewmodel.MainScreenViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
enum class MainScreenPages(
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit,
    val title: String,
    val content: @Composable () -> Unit
) {
    HomePage(
        icon = { Icon(Icons.TwoTone.Home, contentDescription = null, tint = Color.White) },
        label = { Text("Home", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium) },
        title = "Home",
        content = { HomePageContent() }
    ),
    AccountPage(
        icon = { Icon(Icons.TwoTone.AccountCircle, contentDescription = null, tint = Color.White) },
        label = { Text("Account", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium) },
        title = "Account",
        content = { AccountPageContent() }
    ),
    AboutPage(
        icon = { Icon(Icons.AutoMirrored.TwoTone.Help, contentDescription = null, tint = Color.White) },
        label = { Text("About", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium) },
        title = "About",
        content = { AboutPageContent() }
    ),
    SettingsPage(
        icon = { Icon(Icons.TwoTone.Settings, contentDescription = null, tint = Color.White) },
        label = { Text("Settings", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium) },
        title = "Settings",
        content = { SettingsPageContent() }
    )
}

@Composable
fun MainScreen() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val selectedPage by mainScreenViewModel.selectedPage.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // whole background dark
    ) {
        NavigationRailX(
            alignment = Alignment.Top,
            containerColor = Color(0xFF0B0B11)
        ) {
            MainScreenPages.entries.forEach { page ->
                val isSelected = selectedPage == page
                NavigationRailItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) mainScreenViewModel.selectPage(page)
                    },
                    icon = page.icon,
                    label = if (isSelected) page.label else null, // show label only when selected
                    alwaysShowLabel = false
                )
            }
        }

        VerticalDivider(color = Color(0xFF1A1A1A))

        AnimatedContent(
            targetState = selectedPage,
            label = "animatedPage",
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) { currentPage ->
            Box(modifier = Modifier.fillMaxSize()) {
                currentPage.content()
            }
        }
    }
}
