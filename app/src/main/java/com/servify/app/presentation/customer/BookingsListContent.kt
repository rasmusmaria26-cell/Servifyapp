package com.servify.app.presentation.customer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.servify.app.data.model.Booking
import com.servify.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsListContent(
    uiState: CustomerDashboardUiState,
    onBookingClick: (Booking) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Column {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Your Bookings",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = Satoshi,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.error != null && uiState.bookings.isEmpty()) {
            // Error empty state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⚠️", style = MaterialTheme.typography.displaySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.error,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (uiState.bookings.isEmpty() && !uiState.isLoading) {
            // Animated empty state
            AnimatedEmptyState()
        } else {
            // Pull-to-refresh bookings list
            val state = rememberPullToRefreshState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(state.nestedScrollConnection)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.bookings) { booking ->
                        BookingItemCard(booking, onClick = { onBookingClick(booking) })
                    }
                }

                PullToRefreshContainer(
                    state = state,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = DarkSurface,
                    contentColor = ServifyBlue
                )

                LaunchedEffect(state.isRefreshing) {
                    if (state.isRefreshing) {
                        onRefresh()
                    }
                }
            }
        }
    }
}

// ── Animated Empty State ──
@Composable
private fun AnimatedEmptyState() {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_alpha"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = ServifyBlue,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(iconAlpha)
                    .graphicsLayer { translationY = offsetY }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No bookings yet",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Satoshi,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your service bookings will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BookingItemCard(booking: Booking, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.service?.name ?: "Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            getStatusColor(booking.status).copy(alpha = 0.12f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = booking.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(booking.status),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${booking.scheduledDate} at ${booking.scheduledTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = booking.issueDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}
