package com.retrivedmods.luxclient.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Watermark() {
    // Animate the gradient offset
    val transition = rememberInfiniteTransition()
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // arbitrary large shift value
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        )
    )

    // Create a moving dark gradient
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8000FF), // vivid purple
            Color(0xFF3A0CA3), // dark indigo
            Color(0xFF000000)  // black
        ),
        start = androidx.compose.ui.geometry.Offset(shift, 0f),
        end = androidx.compose.ui.geometry.Offset(shift + 200f, 200f)
    )

    // Build styled text
    val richText: AnnotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
            append("LuxClient")
        }
        withStyle(
            style = SpanStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                baselineShift = BaselineShift.Superscript
            )
        ) {
            append(" v1.0.0")
        }
    }

    // UI layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x00000000)) // semi-transparent dark background
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = richText,
                style = androidx.compose.ui.text.TextStyle(
                    brush = gradientBrush,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
