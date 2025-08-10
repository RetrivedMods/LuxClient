package com.retrivedmods.luxclient.overlay

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.retrivedmods.luxclient.game.ModuleCategory
import com.retrivedmods.luxclient.game.ModuleContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class OverlayClickGUI : OverlayWindow() {

    // Enhanced premium color scheme with darker tones
    companion object {
        private val PrimaryAccent = Color(0xFF6A4C93)
        private val SecondaryAccent = Color(0xFF8E5EA2)
        private val DeepBackground = Color(0xFF08080C)  // Even darker
        private val CardBackground = Color(0xFF0A0A10)  // Darker
        private val ModuleBackground = Color(0xFF060609)  // Much darker
        private val SidebarBackground = Color(0xFF0B0B11)  // Darker
        private val AccentGlow = Color(0x4D6A4C93)
        private val TextPrimary = Color(0xFFE5E5E8)
        private val TextSecondary = Color(0xFF9A9AA5)
        private val TextTertiary = Color(0xFF6B6B75)
        private val BorderPrimary = Color(0x306A4C93)
        private val BorderSecondary = Color(0x15FFFFFF)  // More subtle
        private val HoverBackground = Color(0xFF0F0F17)  // Darker
        private val LuxClientGradient1 = Color(0xFF6A4C93)
        private val LuxClientGradient2 = Color(0xFF9B59B6)
        private val LuxClientGradient3 = Color(0xFF8E44AD)
    }

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 30
            }

            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            dimAmount = 0.8f  // Increased dim
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)
    private var isVisible by mutableStateOf(false)

    @Composable
    override fun Content() {
        // Animation state
        LaunchedEffect(Unit) {
            delay(50)
            isVisible = true
        }

        val containerAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "containerAlpha"
        )

        val containerScale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.9f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "containerScale"
        )

        // Smooth undetectable looping background animation
        val infiniteTransition = rememberInfiniteTransition(label = "backgroundGradient")
        val gradientOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(8000)  // Slower for smoother loop
            ),
            label = "gradientAnimation"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Smooth circular motion for undetectable loop
                    val centerX = size.width * (0.5f + 0.3f * sin(gradientOffset * 0.7))
                    val centerY = size.height * (0.5f + 0.2f * sin(gradientOffset))
                    val radius = size.minDimension * (0.6f + 0.2f * sin(gradientOffset * 1.3))

                    val gradientBrush = Brush.radialGradient(
                        colors = listOf(
                            DeepBackground.copy(alpha = 0.99f),
                            Color.Black.copy(alpha = 0.98f),
                            PrimaryAccent.copy(alpha = 0.08f),
                            SecondaryAccent.copy(alpha = 0.04f),
                            Color.Black.copy(alpha = 1f)
                        ),
                        radius = radius.toFloat(),
                        center = Offset(centerX.toFloat(), centerY)
                    )
                    drawRect(gradientBrush)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    closeOverlay()
                },
            contentAlignment = Alignment.Center
        ) {
            // Main container with enhanced premium styling
            Surface(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(0.92f)
                    .graphicsLayer {
                        alpha = containerAlpha
                        scaleX = containerScale
                        scaleY = containerScale
                    }
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = PrimaryAccent,
                        ambientColor = SecondaryAccent
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryAccent.copy(alpha = 0.8f),
                                SecondaryAccent.copy(alpha = 0.4f),
                                Color.Transparent,
                                PrimaryAccent.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent click through */ },
                shape = RoundedCornerShape(24.dp),
                color = CardBackground.copy(alpha = 0.95f)  // Slightly more opaque
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with close button and LuxClient title
                    PremiumHeader()

                    // Main content area
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Navigation rail with premium styling
                        PremiumNavigationRail()

                        // Animated divider
                        AnimatedDivider(gradientOffset)

                        // Content area with smooth transitions
                        PremiumContentArea()
                    }
                }
            }
        }
    }

    private fun closeOverlay() {
        isVisible = false
        CoroutineScope(Dispatchers.Main).launch {
            delay(250)
            OverlayManager.dismissOverlayWindow(this@OverlayClickGUI)
        }
    }

    @Composable
    private fun PremiumHeader() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)  // Increased height for title
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SidebarBackground,
                            ModuleBackground,
                            SidebarBackground
                        )
                    )
                )
        ) {
            // LuxClient title in top-left corner with animations
            val infiniteTransition = rememberInfiniteTransition(label = "titleAnimation")
            val titleGlow by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000)
                ),
                label = "titleGlowAnimation"
            )

            val titleScale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "titleScale"
            )

            Text(
                text = "LuxClient",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 16.dp)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    }
                    .drawBehind {
                        // Subtle glow effect behind text
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryAccent.copy(alpha = 0.3f * titleGlow),
                                    Color.Transparent
                                ),
                                radius = size.width * 0.8f
                            )
                        )
                    },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = TextPrimary.copy(alpha = titleGlow * 0.7f + 0.3f)
            )

            // Animated gradient text effect
            Text(
                text = "LuxClient",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 16.dp)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LuxClientGradient1.copy(alpha = titleGlow),
                            LuxClientGradient2.copy(alpha = titleGlow * 0.8f),
                            LuxClientGradient3.copy(alpha = titleGlow * 0.9f),
                            LuxClientGradient1.copy(alpha = titleGlow)
                        )
                    )
                )
            )

            // Close button in top-right corner
            IconButton(
                onClick = { closeOverlay() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = ModuleBackground.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryAccent.copy(alpha = 0.6f),
                                SecondaryAccent.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    private fun AnimatedDivider(animationOffset: Float) {
        val dividerAlpha = 0.4f + 0.3f * sin(animationOffset * 0.5f)

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BorderSecondary.copy(alpha = dividerAlpha * 0.5f),
                            PrimaryAccent.copy(alpha = dividerAlpha * 0.6f),
                            SecondaryAccent.copy(alpha = dividerAlpha * 0.4f),
                            BorderSecondary.copy(alpha = dividerAlpha * 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }

    @Composable
    private fun PremiumNavigationRail() {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(220.dp),
            color = SidebarBackground.copy(alpha = 0.95f)
        ) {
            // Make the categories scrollable with LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ModuleCategory.entries) { moduleCategory ->
                    PremiumNavigationItem(
                        moduleCategory = moduleCategory,
                        isSelected = selectedModuleCategory === moduleCategory,
                        onClick = {
                            if (selectedModuleCategory !== moduleCategory) {
                                selectedModuleCategory = moduleCategory
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun PremiumNavigationItem(
        moduleCategory: ModuleCategory,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val animatedScale by animateFloatAsState(
            targetValue = if (isSelected) 1.02f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "itemScale"
        )

        val animatedGlow by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = tween(300),
            label = "glowAnimation"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    ModuleBackground.copy(alpha = 0.9f)
                } else {
                    ModuleBackground.copy(alpha = 0.4f)  // Darker unselected state
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryAccent.copy(alpha = 0.9f),
                            SecondaryAccent.copy(alpha = 0.7f),
                            PrimaryAccent.copy(alpha = 0.9f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            BorderSecondary.copy(alpha = 0.3f),  // More subtle border
                            Color.Transparent,
                            BorderSecondary.copy(alpha = 0.3f)
                        )
                    )
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 12.dp else 2.dp  // Less elevation when not selected
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AccentGlow.copy(alpha = 0.3f * animatedGlow),
                                    Color.Transparent,
                                    AccentGlow.copy(alpha = 0.2f * animatedGlow)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DeepBackground.copy(alpha = 0.5f),  // Darker background
                                    Color.Transparent,
                                    DeepBackground.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    .padding(18.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(moduleCategory.iconResId),
                    contentDescription = null,
                    tint = if (isSelected) PrimaryAccent else TextTertiary.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(moduleCategory.labelResId),
                    color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.8f),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }

    @Composable
    private fun PremiumContentArea() {
        AnimatedContent(
            targetState = selectedModuleCategory,
            transitionSpec = {
                (slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { -it / 2 },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400)))
            },
            label = "contentTransition",
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ModuleBackground.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        ) { moduleCategory ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                when (moduleCategory) {
                    ModuleCategory.Config -> {
                        PremiumConfigContent()
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            ModuleContent(moduleCategory)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PremiumConfigContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enhanced config placeholder
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(70.dp),
                        spotColor = PrimaryAccent
                    ),
                shape = RoundedCornerShape(70.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ModuleBackground.copy(alpha = 0.8f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryAccent.copy(alpha = 0.8f),
                            SecondaryAccent.copy(alpha = 0.6f),
                            PrimaryAccent.copy(alpha = 0.8f)
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AccentGlow.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_preferences),
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Configuration Hub",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Premium customization options and\nadvanced client settings coming soon.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.3.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}