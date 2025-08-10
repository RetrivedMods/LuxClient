package com.retrivedmods.luxclient.router.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.retrivedmods.luxclient.R
import com.retrivedmods.luxclient.util.LocalSnackbarHostState
import com.retrivedmods.luxclient.util.SnackbarHostStateScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AboutPageContent() {
    SnackbarHostStateScope {
        val snackbarHostState = LocalSnackbarHostState.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.about))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier
                        .animateContentSize()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CopyrightCard()
                    LoginModeCard()
                    CreditsCard() // New credits section
                }
            }
        }
    }
}

@Composable
private fun CopyrightCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                stringResource(R.string.tips),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.header_introduction),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                stringResource(R.string.copyright),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoginModeCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                stringResource(R.string.how_do_i_switch_login_mode),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.login_mode_introduction),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CreditsCard() {
    val context = LocalContext.current
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Credits",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("LuxClient by RetrivedMods", style = MaterialTheme.typography.bodySmall)
            Text("MuCuteClient", style = MaterialTheme.typography.bodySmall)
            Text("NovaRelay", style = MaterialTheme.typography.bodySmall)
            Text("LuminaClient", style = MaterialTheme.typography.bodySmall)
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RetrivedMods/LuxClient"))
                context.startActivity(intent)
            }) {
                Text("Visit our Open Source Project")
            }
        }
    }
}
