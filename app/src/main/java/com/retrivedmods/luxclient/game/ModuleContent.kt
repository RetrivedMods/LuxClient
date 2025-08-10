package com.retrivedmods.luxclient.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.retrivedmods.luxclient.R
import com.retrivedmods.luxclient.overlay.OverlayManager
import com.retrivedmods.luxclient.util.translatedSelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// Dark theme color scheme matching ClickGUI
object DarkTheme {
    val PrimaryAccent = Color(0xFF6A4C93)
    val SecondaryAccent = Color(0xFF8E5EA2)
    val DeepBackground = Color(0xFF08080C)
    val CardBackground = Color(0xFF0A0A10)
    val ModuleBackground = Color(0xFF060609)
    val ModuleExpanded = Color(0xFF0F0F17)
    val SidebarBackground = Color(0xFF0B0B11)
    val AccentGlow = Color(0x4D6A4C93)
    val TextPrimary = Color(0xFFE5E5E8)
    val TextSecondary = Color(0xFF9A9AA5)
    val TextTertiary = Color(0xFF6B6B75)
    val BorderPrimary = Color(0x306A4C93)
    val BorderSecondary = Color(0x15FFFFFF)
    val HoverBackground = Color(0xFF0F0F17)
    val SurfaceVariant = Color(0xFF12121A)
    val OnSurfaceVariant = Color(0xFFB8B8C0)
    val Outline = Color(0xFF3A3A45)
    val OutlineVariant = Color(0xFF2A2A35)
}

private val moduleCache = HashMap<ModuleCategory, List<Module>>()

private fun fetchCachedModules(moduleCategory: ModuleCategory): List<Module> {
    val cachedModules = moduleCache[moduleCategory] ?: ModuleManager
        .modules
        .filter {
            !it.private && it.category === moduleCategory
        }
    moduleCache[moduleCategory] = cachedModules
    return cachedModules
}

@Composable
fun ModuleContent(moduleCategory: ModuleCategory) {
    var modules: List<Module>? by remember(moduleCategory) { mutableStateOf(moduleCache[moduleCategory]) }

    LaunchedEffect(modules) {
        if (modules == null) {
            withContext(Dispatchers.IO) {
                modules = fetchCachedModules(moduleCategory)
            }
        }
    }

    Crossfade(
        targetState = modules
    ) {
        if (it != null) {
            LazyColumn(
                Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(it.size) { index ->
                    val module = it[index]
                    ModuleCard(module)
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = DarkTheme.PrimaryAccent
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(module: Module) {
    val values = module.values
    val background by animateColorAsState(
        targetValue = if (module.isExpanded) DarkTheme.ModuleExpanded else DarkTheme.ModuleBackground,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "moduleBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (module.isExpanded) DarkTheme.PrimaryAccent else DarkTheme.BorderSecondary,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "moduleBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    module.isExpanded = !module.isExpanded
                }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (module.isExpanded) 1.5.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (module.isExpanded) 8.dp else 4.dp
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier,
                    color = if (module.isExpanded) DarkTheme.TextPrimary else DarkTheme.TextSecondary
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = {
                        module.isEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkTheme.TextPrimary,
                        checkedTrackColor = DarkTheme.PrimaryAccent,
                        checkedBorderColor = DarkTheme.PrimaryAccent,
                        uncheckedThumbColor = DarkTheme.TextTertiary,
                        uncheckedTrackColor = DarkTheme.OutlineVariant,
                        uncheckedBorderColor = DarkTheme.Outline,
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }
            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(it)
                        is FloatValue -> FloatValueContent(it)
                        is IntValue -> IntValueContent(it)
                        is ListValue -> ChoiceValueContent(it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkTheme.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            value.listItems.forEach {
                ElevatedFilterChip(
                    selected = value.value == it,
                    onClick = {
                        if (value.value != it) {
                            value.value = it
                        }
                    },
                    label = {
                        Text(
                            it.name.translatedSelf,
                            color = if (value.value == it) DarkTheme.TextPrimary else DarkTheme.TextSecondary
                        )
                    },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        containerColor = DarkTheme.SurfaceVariant,
                        selectedContainerColor = DarkTheme.PrimaryAccent.copy(alpha = 0.8f),
                        selectedLabelColor = DarkTheme.TextPrimary,
                        labelColor = DarkTheme.TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = value.value == it,
                        borderColor = if (value.value == it) DarkTheme.PrimaryAccent else DarkTheme.Outline,
                        selectedBorderColor = DarkTheme.PrimaryAccent,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.5.dp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatValueContent(value: FloatValue) {
    Column(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTheme.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                String.format("%.2f", value.value),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTheme.PrimaryAccent
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val colors = SliderDefaults.colors(
            thumbColor = DarkTheme.PrimaryAccent,
            activeTrackColor = DarkTheme.PrimaryAccent,
            activeTickColor = DarkTheme.PrimaryAccent,
            inactiveTickColor = DarkTheme.OutlineVariant,
            inactiveTrackColor = DarkTheme.OutlineVariant
        )
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = animateFloatAsState(
                targetValue = value.value,
                label = "",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            ).value,
            valueRange = value.range,
            colors = colors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(6.dp, 24.dp),
                    enabled = true
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = true,
                    sliderState = sliderState,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 6.dp
                )
            },
            onValueChange = {
                val newValue = ((it * 100.0).roundToInt() / 100.0).toFloat()
                if (value.value != newValue) {
                    value.value = newValue
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntValueContent(value: IntValue) {
    Column(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTheme.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTheme.PrimaryAccent
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val colors = SliderDefaults.colors(
            thumbColor = DarkTheme.PrimaryAccent,
            activeTrackColor = DarkTheme.PrimaryAccent,
            activeTickColor = DarkTheme.PrimaryAccent,
            inactiveTickColor = DarkTheme.OutlineVariant,
            inactiveTrackColor = DarkTheme.OutlineVariant
        )
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = animateFloatAsState(
                targetValue = value.value.toFloat(),
                label = "",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            ).value,
            valueRange = value.range.toFloatRange(),
            colors = colors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(6.dp, 24.dp),
                    enabled = true
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = true,
                    sliderState = sliderState,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 6.dp
                )
            },
            onValueChange = {
                val newValue = it.roundToInt()
                if (value.value != newValue) {
                    value.value = newValue
                }
            }
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .toggleable(
                value = value.value,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    value.value = it
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkTheme.TextSecondary
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = DarkTheme.Outline,
                checkedColor = DarkTheme.PrimaryAccent,
                checkmarkColor = DarkTheme.TextPrimary,
                disabledCheckedColor = DarkTheme.OutlineVariant,
                disabledUncheckedColor = DarkTheme.OutlineVariant
            )
        )
    }
}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .toggleable(
                value = module.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    module.isShortcutDisplayed = it
                    if (it) {
                        OverlayManager.showOverlayWindow(module.overlayShortcutButton)
                    } else {
                        OverlayManager.dismissOverlayWindow(module.overlayShortcutButton)
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = DarkTheme.TextTertiary
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = DarkTheme.Outline,
                checkedColor = DarkTheme.SecondaryAccent,
                checkmarkColor = DarkTheme.TextPrimary,
                disabledCheckedColor = DarkTheme.OutlineVariant,
                disabledUncheckedColor = DarkTheme.OutlineVariant
            )
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()