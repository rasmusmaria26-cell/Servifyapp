package com.servify.app.presentation.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.presentation.components.ServifyButton
import com.servify.app.presentation.components.ShimmerItem
import com.servify.app.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// ── Constants ─────────────────────────────────────────────────────────────────

private val DEVICE_TYPES = listOf(
    "Smartphone", "Laptop", "Tablet", "Desktop PC",
    "Air Conditioner", "Washing Machine", "Refrigerator",
    "Television", "Microwave", "Other"
)

private val ISSUE_CATEGORIES = listOf(
    "Screen / Display", "Battery", "Charging Port",
    "Software / OS Crash", "Water Damage", "Speaker / Mic",
    "Keyboard / Trackpad", "Cooling / Fan", "Power (Won't turn on)",
    "Camera", "Other"
)

private val SEVERITY_OPTIONS = listOf(
    Triple("MINOR",    "Minor",    Color(0xFF4CAF50)),   // green
    Triple("MODERATE", "Moderate", Color(0xFFFFC107)),   // amber
    Triple("SEVERE",   "Severe",   Color(0xFFF44336)),   // red
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRepairRequestScreen(
    viewModel: PostRepairRequestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSubmitted: (requestId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Guard against double-navigation if the user somehow taps Continue twice
    var hasNavigated by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Post Repair Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Section: Device Info ──────────────────────────────────────────
            FormSection(title = "Device Info", icon = Icons.Default.Devices) {
                // Device Type picker
                DropdownField(
                    label = "Device Type *",
                    value = uiState.deviceType,
                    options = DEVICE_TYPES,
                    onSelect = viewModel::onDeviceType
                )
                Spacer(Modifier.height(10.dp))
                DarkTextField(
                    label = "Brand (e.g. Apple, Samsung)",
                    value = uiState.brand,
                    onValueChange = viewModel::onBrand
                )
                Spacer(Modifier.height(10.dp))
                DarkTextField(
                    label = "Model (e.g. iPhone 14)",
                    value = uiState.model,
                    onValueChange = viewModel::onModel
                )
            }

            // ── Section: Issue Details ────────────────────────────────────────
            FormSection(title = "Issue Details", icon = Icons.Default.BugReport) {
                DropdownField(
                    label = "Issue Category *",
                    value = uiState.issueCategory,
                    options = ISSUE_CATEGORIES,
                    onSelect = viewModel::onIssueCategory
                )
                Spacer(Modifier.height(14.dp))

                // Severity pills
                Text(text = "Severity *", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, fontFamily = Inter)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SEVERITY_OPTIONS.forEach { (key, label, color) ->
                        val selected = uiState.severity == key
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) color else DarkBorder,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { viewModel.onSeverity(key) }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (selected) color else TextSecondary,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = Inter,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Section: Description ──────────────────────────────────────────
            FormSection(title = "Description", icon = Icons.Default.Description) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescription,
                    placeholder = { Text(text = "Describe the problem in detail...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = Inter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = MaterialTheme.colorScheme.onBackground)
                )
            }

            // ── Section: Media ────────────────────────────────────────────────
            FormSection(title = "Photos / Videos", icon = Icons.Default.PhotoCamera) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { /* TODO: launch media picker */ }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tap to add photos or video",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = Inter
                        )
                    }
                }
            }

            // ── Section: Location ─────────────────────────────────────────────
            FormSection(title = "Location", icon = Icons.Default.LocationOn) {
                val context = LocalContext.current
                val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    viewModel.onLocationSelected(location.latitude, location.longitude)
                                }
                            }
                        } catch (e: SecurityException) { }
                    }
                }
                
                fun requestLocation() {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    viewModel.onLocationSelected(location.latitude, location.longitude)
                                }
                            }
                        } catch (e: SecurityException) { }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                }

                LaunchedEffect(Unit) {
                    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
                }

                Text(
                    text = "Drag the map to pinpoint your location.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    val lat = uiState.latitude
                    val lng = uiState.longitude
                    
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                val initialPoint = GeoPoint(lat ?: 28.6139, lng ?: 77.2090)
                                controller.setCenter(initialPoint)

                                addMapListener(object : org.osmdroid.events.MapListener {
                                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                                        val center = mapCenter
                                        viewModel.onLocationSelected(center.latitude, center.longitude)
                                        return true
                                    }
                                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?) = false
                                })
                            }
                        },
                        update = { view ->
                            if (lat != null && lng != null) {
                                val dist = Math.abs(view.mapCenter.latitude - lat) + Math.abs(view.mapCenter.longitude - lng)
                                if (dist > 0.0001) {
                                    view.controller.animateTo(GeoPoint(lat, lng))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    // Center pin icon
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pinpoint",
                        tint = ErrorRed,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .offset(y = (-18).dp)
                    )
                    
                    // Locate Me Button
                    SmallFloatingActionButton(
                        onClick = { requestLocation() },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = ServifyBlue,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Locate Me")
                    }
                }

                Spacer(Modifier.height(16.dp))
                DarkTextField(
                    label = "House/Flat No., Landmark",
                    value = uiState.address,
                    onValueChange = viewModel::onAddress
                )
            }

            // ── Error message ─────────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = Inter,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // ── CTA ───────────────────────────────────────────────────────────
            // Show the submit button only while the form hasn't been submitted yet
            if (uiState.submittedRequestId == null) {
                ServifyButton(
                    text = if (uiState.isSubmitting) "Posting Request…" else "Post Request — Get Quotes",
                    onClick = viewModel::submit,
                    isLoading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )

                // Note about 60-min window
                Text(
                    text = "🕐 Vendors have 60 minutes to submit quotes after you post.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Inter,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            // ── Post-submission: diagnosis shimmer / card / continue ───────────
            if (uiState.submittedRequestId != null) {

                // 1. Shimmer while the AI call is in flight
                if (uiState.isDiagnosing) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Analysing your issue with AI…",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = Inter,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ShimmerItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }

                // 2. Diagnosis card once available
                uiState.diagnosis?.let { diagnosis ->
                    DiagnosisResultCard(diagnosis = diagnosis)
                }

                // 3. Error fallback (diagnosis unavailable) — still show Continue
                if (uiState.diagnosisError != null && uiState.diagnosis == null && !uiState.isDiagnosing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Diagnosis unavailable. Continue to see vendor quotes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = Inter,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // 4. Continue button — shown once diagnosing is done (result or error)
                if (!uiState.isDiagnosing) {
                    Spacer(Modifier.height(4.dp))
                    ServifyButton(
                        text = "Continue →",
                        onClick = {
                            if (!hasNavigated) {
                                hasNavigated = true
                                onSubmitted(uiState.submittedRequestId!!)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Reusable Form Components ──────────────────────────────────────────────────

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontFamily = SpaceGrotesk, 
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun DarkTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium, fontFamily = Inter, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = MaterialTheme.colorScheme.onBackground)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value.ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label, style = MaterialTheme.typography.labelMedium, fontFamily = Inter, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Inter, color = MaterialTheme.colorScheme.onBackground),
            placeholder = { Text(text = "Select…", style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, color = MaterialTheme.colorScheme.onBackground) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
