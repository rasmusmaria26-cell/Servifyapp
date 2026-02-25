package com.servify.app.presentation.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.ui.theme.*
import com.servify.app.presentation.components.AmbientGlow
import com.servify.app.presentation.components.ServifySearchField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CustomerDashboardScreen(
    viewModel: CustomerDashboardViewModel = hiltViewModel(),
    onNavigateToBooking: () -> Unit,
    onNavigateToBookingDetail: (String) -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Observe sign-out event
    val signedOut by viewModel.signedOut.collectAsState()
    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontFamily = Satoshi, fontWeight = FontWeight.Medium) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ServifyBlue,
                        selectedTextColor = ServifyBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ServifyBlue.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        val pendingCount = uiState.bookings.count { it.status == "PENDING" }
                        if (pendingCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = ServifyBlue,
                                        contentColor = Color.White
                                    ) {
                                        Text(pendingCount.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.History, contentDescription = "Bookings")
                            }
                        } else {
                            Icon(Icons.Default.History, contentDescription = "Bookings")
                        }
                    },
                    label = { Text("Orders", fontFamily = Satoshi, fontWeight = FontWeight.Medium) },
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        selectedTab = 1
                        viewModel.fetchBookings()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ServifyBlue,
                        selectedTextColor = ServifyBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ServifyBlue.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontFamily = Satoshi, fontWeight = FontWeight.Medium) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ServifyBlue,
                        selectedTextColor = ServifyBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ServifyBlue.copy(alpha = 0.12f)
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onNavigateToBooking,
                    containerColor = ServifyBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Booking")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Subtle ambient glow at top
            AmbientGlow()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(300),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreenContent(uiState, onNavigateToBooking)
                        1 -> BookingsListContent(
                            uiState = uiState,
                            onBookingClick = { booking ->
                                viewModel.selectBooking(booking)
                                onNavigateToBookingDetail(booking.id)
                            },
                            onRefresh = { viewModel.fetchBookings() }
                        )
                        2 -> ProfileScreenContent(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    uiState: CustomerDashboardUiState,
    onNavigateToBooking: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val services = listOf(
        ServiceCategory("Electronics", Icons.Default.Smartphone, ElectronicsBlue, "Phones, Laptops, Gadgets"),
        ServiceCategory("Mechanical", Icons.Default.DirectionsCar, MechanicalAmber, "Cars, Bikes, Vehicles"),
        ServiceCategory("Home Services", Icons.Default.Home, HomeEmerald, "Appliances, Plumbing, AC")
    )

    val filteredServices = services.filter { service ->
        service.name.contains(searchQuery, ignoreCase = true) ||
        service.description.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Hero Section
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontFamily = Satoshi
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What needs fixing?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = Satoshi,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick = { /* Settings */ },
                    modifier = Modifier
                        .size(44.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, DarkBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ServifySearchField(
                value = searchQuery,
                onValueChange = { query: String -> searchQuery = query },
                placeholder = "What needs repair?"
            )
        }

        // Section Header
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Services",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Satoshi,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Full-Width Horizontal Service Cards with Staggered Animation
        itemsIndexed(filteredServices) { index, service ->
            val animatedAlpha = remember { Animatable(0f) }
            val animatedOffset = remember { Animatable(30f) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                launch { animatedAlpha.animateTo(1f, tween(400)) }
                launch { animatedOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
            }

            ServiceRow(
                service = service,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onNavigateToBooking()
                },
                modifier = Modifier
                    .graphicsLayer {
                        alpha = animatedAlpha.value
                        translationY = animatedOffset.value
                    }
            )
            if (index < filteredServices.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileScreenContent(viewModel: CustomerDashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    // Fade-in
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(400))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = contentAlpha.value },
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Avatar + Name + Role
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle with initials
                val initials = profile?.fullName?.split(" ")
                    ?.take(2)?.joinToString("") { it.take(1).uppercase() } ?: "?"
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ServifyBlue.copy(alpha = 0.15f))
                        .border(2.dp, ServifyBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = Satoshi,
                        fontWeight = FontWeight.Bold,
                        color = ServifyBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.fullName ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Satoshi,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role badge
                Box(
                    modifier = Modifier
                        .background(ServifyBlue.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (profile?.role ?: "customer").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = ServifyBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Satoshi
                    )
                }
            }
        }

        // Stats Row
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Bookings
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.bookings.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ServifyBlue,
                            fontFamily = Satoshi
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Member Since
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile?.createdAt?.take(10) ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = Satoshi
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Member Since",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Settings Section
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Satoshi,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Settings rows
        val settingsItems = listOf(
            Triple(Icons.Default.Notifications, "Notifications", "Manage alerts"),
            Triple(Icons.Default.CreditCard, "Payment Methods", "Cards & UPI"),
            Triple(Icons.Default.HelpOutline, "Help & Support", "FAQs & Contact"),
            Triple(Icons.Default.Info, "About Servify", "Version 1.0")
        )

        items(settingsItems.size) { index ->
            val (icon, title, subtitle) = settingsItems[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO */ },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontFamily = Satoshi, fontWeight = FontWeight.SemiBold)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
            if (index < settingsItems.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Sign Out Button
        item {
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedButton(
                onClick = { viewModel.signOut() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign Out",
                    fontFamily = Satoshi,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "PENDING" -> AmberAccent
        "ACCEPTED" -> Color(0xFF22C55E)
        "COMPLETED" -> ServifyBlue
        "CANCELLED" -> ErrorRed
        else -> TextSecondary
    }
}

data class ServiceCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

// ==========================================================
// Full-Width Horizontal Service Row (Option C layout)
// ==========================================================
@Composable
fun ServiceRow(
    service: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "row_scale"
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "row_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = pressAlpha
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored icon container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(service.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = service.color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title + Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Satoshi,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
