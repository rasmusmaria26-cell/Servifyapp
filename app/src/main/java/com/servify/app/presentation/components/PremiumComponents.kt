package com.servify.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.servify.app.ui.theme.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor

// ==========================================================
// Ambient Background Glow — subtle radial glow at top center
// ==========================================================
@Composable
fun AmbientGlow(
    modifier: Modifier = Modifier,
    color: Color = ServifyBlue
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = 600f
                )
            )
    )
}

// ==========================================================
// Search Field with focus glow animation
// ==========================================================
@Composable
fun ServifySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for services..."
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 1.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "search_border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceLight)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, ServifyBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    fontFamily = Satoshi
                ),
                singleLine = true,
                cursorBrush = SolidColor(ServifyBlue),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            fontFamily = Satoshi
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
