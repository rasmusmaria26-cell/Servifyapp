package com.servify.app.presentation.customer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.servify.app.data.model.Booking
import com.servify.app.presentation.components.ServifyButton
import com.servify.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: Booking,
    onNavigateBack: () -> Unit
) {
    // Fade-in animation
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(400))
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Booking Details",
                        fontFamily = Satoshi,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = contentAlpha.value }
        ) {
            // Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status badge
                    val statusColor = getStatusColor(booking.status)
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = booking.status,
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Satoshi
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = booking.service?.name ?: "Service",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = Satoshi
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Booking #${booking.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Issue Description
            SectionCard(
                title = "Issue Description",
                icon = Icons.Default.Description
            ) {
                Text(
                    text = booking.issueDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schedule & Location
            SectionCard(
                title = "Schedule & Location",
                icon = Icons.Default.CalendarToday
            ) {
                DetailRow(Icons.Default.Event, "Date", booking.scheduledDate)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(Icons.Default.Schedule, "Time", booking.scheduledTime)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(Icons.Default.LocationOn, "Address", booking.address)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cost Breakdown
            SectionCard(
                title = "Cost",
                icon = Icons.Default.CurrencyRupee
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                        text = if (booking.estimatedPrice != null) "₹${booking.estimatedPrice}" else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (booking.finalPrice != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "₹${booking.finalPrice}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ServifyBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Status
            SectionCard(
                title = "Payment",
                icon = Icons.Default.Payment
            ) {
                val paymentColor = if (booking.paymentStatus == "PAID") Color(0xFF22C55E) else AmberAccent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .background(paymentColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = booking.paymentStatus,
                            style = MaterialTheme.typography.labelMedium,
                            color = paymentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // AI Diagnosis (if available)
            if (booking.aiDiagnosis != null) {
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(
                    title = "AI Diagnosis",
                    icon = Icons.Default.AutoAwesome
                ) {
                    Text(
                        text = booking.aiDiagnosis.diagnosis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    if (booking.aiDiagnosis.possibleCauses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Possible Causes",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        booking.aiDiagnosis.possibleCauses.forEach { cause ->
                            Text(
                                text = "• $cause",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Rating Section (if completed)
            if (booking.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(
                    title = "Rate Service",
                    icon = Icons.Default.Star
                ) {
                    var rating by remember { mutableIntStateOf(0) }
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) AmberAccent else TextSecondary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        rating = i
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    }
                            )
                        }
                    }
                    if (rating > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ServifyButton(
                            text = "Submit Rating",
                            onClick = { /* TODO */ },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Section Card Wrapper ──
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = ServifyBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = Satoshi
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// ── Detail Row ──
@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
