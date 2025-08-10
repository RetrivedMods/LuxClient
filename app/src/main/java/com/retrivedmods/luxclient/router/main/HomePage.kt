package com.retrivedmods.luxclient.router.main

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radiantbyte.novarelay.NovaRelay
import com.retrivedmods.luxclient.R
import com.retrivedmods.luxclient.service.Services
import com.retrivedmods.luxclient.util.LocalSnackbarHostState
import com.retrivedmods.luxclient.util.SnackbarHostStateScope
import com.retrivedmods.luxclient.viewmodel.MainScreenViewModel
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageContent() {
    // Premium color scheme
    val PrimaryAccent = Color(0xFF6A4C93)
    val SecondaryAccent = Color(0xFF8E5EA2)
    val DeepBackground = Color(0xFF08080C)
    val CardBackground = Color(0xFF0A0A10)
    val ModuleBackground = Color(0xFF060609)
    val SidebarBackground = Color(0xFF0B0B11)
    val AccentGlow = Color(0x4D6A4C93)
    val TextPrimary = Color(0xFFE5E5E8)
    val TextSecondary = Color(0xFF9A9AA5)
    val TextTertiary = Color(0xFF6B6B75)
    val BorderPrimary = Color(0x306A4C93)
    val BorderSecondary = Color(0x15FFFFFF)
    val LuxClientGradient1 = Color(0xFF6A4C93)
    val LuxClientGradient2 = Color(0xFF9B59B6)
    val LuxClientGradient3 = Color(0xFF8E44AD)

    // Background animation
    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(8000)),
        label = "backgroundAnimation"
    )

    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = LocalSnackbarHostState.current
        val mainScreenViewModel: MainScreenViewModel = viewModel()
        val onPostPermissionResult: (Boolean) -> Unit = block@{ isGranted: Boolean ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.notification_permission_denied)
                    )
                }
                return@block
            }

            if (mainScreenViewModel.selectedGame.value === null) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.select_game_first)
                    )
                }
                return@block
            }

            Services.toggle(context, mainScreenViewModel.captureModeModel.value)
        }

        val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> onPostPermissionResult(isGranted) }

        val overlayPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (!Settings.canDrawOverlays(context)) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.overlay_permission_denied)
                    )
                }
                return@rememberLauncherForActivityResult
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@rememberLauncherForActivityResult
            }
            onPostPermissionResult(true)
        }

        var isActiveBefore by rememberSaveable { mutableStateOf(Services.isActive) }
        var showConnectionDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Services.isActive) {
            if (Services.isActive == isActiveBefore) {
                return@LaunchedEffect
            }

            isActiveBefore = Services.isActive
            if (Services.isActive) {
                showConnectionDialog = true
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.backend_connected),
                    actionLabel = context.getString(R.string.start_game)
                )
                val selectedGame = mainScreenViewModel.selectedGame.value
                if (result == SnackbarResult.ActionPerformed && selectedGame != null) {
                    val intent = context.packageManager.getLaunchIntentForPackage(selectedGame)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.failed_to_launch_game),
                        )
                    }
                }
                return@LaunchedEffect
            }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.backend_disconnected)
            )
        }

        // Connection Dialog
        if (showConnectionDialog) {
            val ipAddress = remember {
                runCatching {
                    NetworkInterface.getNetworkInterfaces().asSequence()
                        .flatMap { it.inetAddresses.asSequence() }
                        .filterIsInstance<Inet4Address>()
                        .firstOrNull { !it.isLoopbackAddress }
                        ?.hostAddress
                }.getOrNull() ?: "127.0.0.1"
            }

            AlertDialog(
                onDismissRequest = { showConnectionDialog = false },
                title = {
                    Text(
                        "How to Connect",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "To join, go to Minecraft's Friends tab and join through LAN. If LAN doesn't show up, you can add a new server in the Servers tab by entering the IP address and port provided below, then press Play.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )

                        // IP Address Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "IP Address:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    ipAddress,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = PrimaryAccent,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("IP Address", ipAddress)
                                        clipboard.setPrimaryClip(clip)

                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("IP Address copied to clipboard")
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy IP Address",
                                        tint = PrimaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Port Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Port:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                "19132",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = PrimaryAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showConnectionDialog = false }
                    ) {
                        Text(
                            "OK",
                            color = PrimaryAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = CardBackground,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "LuxClient",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(LuxClientGradient1, LuxClientGradient2)
                                    )
                                )
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CardBackground.copy(alpha = 0.9f),
                            titleContentColor = TextPrimary
                        ),
                        modifier = Modifier.shadow(8.dp)
                    )
                },
                bottomBar = {
                    SnackbarHost(
                        snackbarHostState,
                        modifier = Modifier.animateContentSize()
                    )
                },
                containerColor = Color.Transparent,
                floatingActionButton = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val fabScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.9f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )

                    FloatingActionButton(
                        onClick = {
                            if (!Settings.canDrawOverlays(context)) {
                                Toast.makeText(
                                    context,
                                    R.string.request_overlay_permission,
                                    Toast.LENGTH_SHORT
                                ).show()

                                overlayPermissionLauncher.launch(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package: ${context.packageName}")
                                    )
                                )
                                return@FloatingActionButton
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@FloatingActionButton
                            }

                            onPostPermissionResult(true)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .graphicsLayer { scaleX = fabScale; scaleY = fabScale }
                            .shadow(12.dp, shape = CircleShape, spotColor = PrimaryAccent),
                        containerColor = PrimaryAccent,
                        contentColor = Color.White,
                        interactionSource = interactionSource
                    ) {
                        AnimatedContent(Services.isActive, label = "") { isActive ->
                            Icon(
                                if (!isActive) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BackendCard(
                        primaryAccent = PrimaryAccent,
                        cardBackground = CardBackground,
                        textPrimary = TextPrimary,
                        textSecondary = TextSecondary,
                        borderPrimary = BorderPrimary
                    )

                    GameCard(
                        cardBackground = CardBackground,
                        textPrimary = TextPrimary,
                        textSecondary = TextSecondary,
                        borderPrimary = BorderPrimary,
                        primaryAccent = PrimaryAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun BackendCard(
    primaryAccent: Color,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderPrimary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground.copy(alpha = 0.9f),
            contentColor = textPrimary
        ),
        border = BorderStroke(1.dp, borderPrimary),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Plumbing,
                    contentDescription = null,
                    tint = primaryAccent,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    stringResource(R.string.backend),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Text(
                stringResource(R.string.backend_introduction),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textSecondary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun GameCard(
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderPrimary: Color,
    primaryAccent: Color
) {
    val context = LocalContext.current
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsStateWithLifecycle()
    var showGameSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var serverHostName by rememberSaveable(showGameSettingsDialog) { mutableStateOf(captureModeModel.serverHostName) }
    var serverPort by rememberSaveable(showGameSettingsDialog) { mutableStateOf(captureModeModel.serverPort.toString()) }
    var showGameSelectorDialog by remember { mutableStateOf(false) }
    val packageInfos by mainScreenViewModel.packageInfos.collectAsStateWithLifecycle()
    val packageInfoState by mainScreenViewModel.packageInfoState.collectAsStateWithLifecycle()
    val selectedGame by mainScreenViewModel.selectedGame.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    // Fixed icon implementation with fallback
    val minecraftIcon = painterResource(id = R.mipmap.minecraft_icon)
    val iconModifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(8.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground.copy(alpha = 0.9f),
            contentColor = textPrimary
        ),
        border = BorderStroke(1.dp, borderPrimary),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = { showGameSettingsDialog = true },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Improved icon display with error handling
            if (minecraftIcon.intrinsicSize.width > 0) {
                Image(
                    painter = minecraftIcon,
                    contentDescription = "Minecraft Icon",
                    modifier = iconModifier
                )
            } else {
                Box(
                    modifier = iconModifier
                        .background(cardBackground.copy(alpha = 0.7f))
                        .border(1.dp, borderPrimary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Default Icon",
                        tint = textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.minecraft),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    stringResource(
                        R.string.recommended_version,
                        NovaRelay.DefaultCodec.minecraftVersion
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textSecondary
                    )
                )
            }
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = primaryAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showGameSelectorDialog) {
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            mainScreenViewModel.fetchPackageInfos()
        }

        BasicAlertDialog(
            onDismissRequest = { showGameSelectorDialog = false },
            modifier = Modifier.padding(vertical = 24.dp),
            content = {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 24.dp,
                    color = cardBackground,
                    border = BorderStroke(1.dp, borderPrimary)
                ) {
                    Column(
                        Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.game_selector),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        )
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                if (packageInfoState === MainScreenViewModel.PackageInfoState.Loading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .padding(vertical = 16.dp)
                                            .fillMaxWidth(),
                                        color = primaryAccent
                                    )
                                }
                            }
                            items(packageInfos.size) {
                                val packageInfo = packageInfos[it]
                                val applicationInfo = packageInfo.applicationInfo!!
                                val packageManager = context.packageManager
                                val icon = remember {
                                    applicationInfo.loadIcon(packageManager).toBitmap()
                                        .asImageBitmap()
                                }
                                val name = remember {
                                    applicationInfo.loadLabel(packageManager).toString()
                                }
                                val packageName = packageInfo.packageName
                                val versionName = packageInfo.versionName ?: "0.0.0"

                                val itemInteractionSource = remember { MutableInteractionSource() }
                                val isItemPressed by itemInteractionSource.collectIsPressedAsState()
                                val itemScale by animateFloatAsState(
                                    targetValue = if (isItemPressed) 0.95f else 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )

                                Card(
                                    onClick = {
                                        mainScreenViewModel.selectGame(packageName)
                                        showGameSelectorDialog = false
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = cardBackground.copy(alpha = 0.8f),
                                        contentColor = textPrimary
                                    ),
                                    border = BorderStroke(1.dp, borderPrimary),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    modifier = Modifier.graphicsLayer { scaleX = itemScale; scaleY = itemScale },
                                    interactionSource = itemInteractionSource
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            bitmap = icon,
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = textPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                packageName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = textSecondary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                versionName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = textSecondary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (showGameSettingsDialog) {
        BasicAlertDialog(
            onDismissRequest = { showGameSettingsDialog = false },
            modifier = Modifier.padding(vertical = 24.dp),
            content = {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 24.dp,
                    color = cardBackground,
                    border = BorderStroke(1.dp, borderPrimary)
                ) {
                    Column(
                        Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.game_settings),
                            modifier = Modifier.align(Alignment.Start),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            val gameInteractionSource = remember { MutableInteractionSource() }
                            val isGamePressed by gameInteractionSource.collectIsPressedAsState()
                            if (isGamePressed) {
                                SideEffect {
                                    showGameSelectorDialog = true
                                }
                            }

                            TextField(
                                value = selectedGame ?: "",
                                onValueChange = {},
                                readOnly = true,
                                maxLines = 1,
                                label = {
                                    Text(
                                        stringResource(R.string.select_game),
                                        color = textSecondary
                                    )
                                },
                                placeholder = {
                                    Text(
                                        stringResource(R.string.no_game_selected),
                                        color = textSecondary.copy(alpha = 0.7f)
                                    )
                                },
                                interactionSource = gameInteractionSource,
                                enabled = !Services.isActive,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                    unfocusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                    disabledContainerColor = cardBackground.copy(alpha = 0.5f),
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    disabledTextColor = textPrimary.copy(alpha = 0.5f),
                                    focusedIndicatorColor = primaryAccent,
                                    unfocusedIndicatorColor = borderPrimary,
                                    disabledIndicatorColor = borderPrimary.copy(alpha = 0.3f),
                                    focusedLabelColor = textSecondary,
                                    unfocusedLabelColor = textSecondary,
                                    disabledLabelColor = textSecondary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TextField(
                                    value = serverHostName,
                                    label = {
                                        Text(
                                            stringResource(R.string.server_host_name),
                                            color = textSecondary
                                        )
                                    },
                                    onValueChange = {
                                        serverHostName = it
                                        if (it.isEmpty()) return@TextField
                                        mainScreenViewModel.selectCaptureModeModel(
                                            captureModeModel.copy(serverHostName = it)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    singleLine = true,
                                    enabled = !Services.isActive,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                        unfocusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                        disabledContainerColor = cardBackground.copy(alpha = 0.5f),
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        disabledTextColor = textPrimary.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryAccent,
                                        unfocusedIndicatorColor = borderPrimary,
                                        disabledIndicatorColor = borderPrimary.copy(alpha = 0.3f),
                                        focusedLabelColor = textSecondary,
                                        unfocusedLabelColor = textSecondary,
                                        disabledLabelColor = textSecondary.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                TextField(
                                    value = serverPort,
                                    label = {
                                        Text(
                                            stringResource(R.string.server_port),
                                            color = textSecondary
                                        )
                                    },
                                    onValueChange = {
                                        serverPort = it
                                        if (it.isEmpty()) return@TextField
                                        val port = it.toIntOrNull() ?: return@TextField
                                        if (port < 0 || port > 65535) return@TextField
                                        mainScreenViewModel.selectCaptureModeModel(
                                            captureModeModel.copy(serverPort = port)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    singleLine = true,
                                    enabled = !Services.isActive,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                        unfocusedContainerColor = cardBackground.copy(alpha = 0.8f),
                                        disabledContainerColor = cardBackground.copy(alpha = 0.5f),
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        disabledTextColor = textPrimary.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryAccent,
                                        unfocusedIndicatorColor = borderPrimary,
                                        disabledIndicatorColor = borderPrimary.copy(alpha = 0.3f),
                                        focusedLabelColor = textSecondary,
                                        unfocusedLabelColor = textSecondary,
                                        disabledLabelColor = textSecondary.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (Services.isActive) {
                                Card(
                                    modifier = Modifier
                                        .width(TextFieldDefaults.MinWidth),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = cardBackground.copy(alpha = 0.8f),
                                        contentColor = textPrimary
                                    ),
                                    border = BorderStroke(1.dp, borderPrimary)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = primaryAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                stringResource(R.string.tips),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = textPrimary
                                                )
                                            )
                                            Text(
                                                stringResource(R.string.change_game_settings_tip),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = textSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}