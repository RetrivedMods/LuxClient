package com.retrivedmods.luxclient.router.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retrivedmods.luxclient.R
import com.retrivedmods.luxclient.game.ModuleManager
import com.retrivedmods.luxclient.util.LocalSnackbarHostState
import com.retrivedmods.luxclient.util.SnackbarHostStateScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContent() {
    // Simple Dark Colors
    val darkBg = Color(0xFF121212)
    val cardBg = Color(0xFF1E1E1E)
    val primary = Color(0xFF4CAF50)
    val secondary = Color(0xFF2196F3)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB0B0B0)

    SnackbarHostStateScope {
        val context = LocalContext.current
        val snackbarHostState = LocalSnackbarHostState.current
        val coroutineScope = rememberCoroutineScope()
        var showFileNameDialog by remember { mutableStateOf(false) }
        var configFileName by remember { mutableStateOf("") }
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val filePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                if (ModuleManager.importConfigFromFile(context, it)) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("✅ Config imported")
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("❌ Import failed")
                    }
                }
            }
        }

        Scaffold(
            containerColor = darkBg,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = primary
                            )
                            Text(
                                stringResource(R.string.settings),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBg
                    )
                )
            },
            bottomBar = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Manage Configs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        )

                        Button(
                            onClick = { filePickerLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondary
                            )
                        ) {
                            Icon(Icons.Rounded.Upload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Import Config")
                        }

                        Button(
                            onClick = { showFileNameDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary
                            )
                        ) {
                            Icon(Icons.Rounded.SaveAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export Config")
                        }
                    }
                }
            }

            if (showFileNameDialog) {
                AlertDialog(
                    onDismissRequest = { showFileNameDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                val success = ModuleManager.exportConfigToFile(context, configFileName)
                                coroutineScope.launch {
                                    if (success) {
                                        snackbarHostState.showSnackbar("✅ Config exported")
                                    } else {
                                        snackbarHostState.showSnackbar("❌ Export failed")
                                    }
                                }
                                showFileNameDialog = false
                            },
                            enabled = configFileName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary
                            )
                        ) {
                            Text("Export")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFileNameDialog = false }) {
                            Text("Cancel", color = textSecondary)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.SaveAlt,
                            contentDescription = null,
                            tint = primary
                        )
                    },
                    title = {
                        Text(
                            "Export Config",
                            color = textPrimary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter filename:",
                                color = textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = configFileName,
                                onValueChange = { configFileName = it },
                                label = { Text("Filename", color = textSecondary) },
                                placeholder = { Text("config.json", color = textSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = primary,
                                    unfocusedBorderColor = textSecondary
                                )
                            )
                        }
                    },
                    containerColor = cardBg
                )
            }
        }
    }
}