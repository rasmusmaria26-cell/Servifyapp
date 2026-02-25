package com.servify.app.presentation.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.data.model.Vendor
import com.servify.app.presentation.components.ServifyButton
import com.servify.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    viewModel: CreateBookingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onBookingCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.bookingCreated) {
        if (uiState.bookingCreated) {
            onBookingCreated()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Booking",
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
        ) {
            // Step Progress Indicator
            BookingStepIndicator(currentStep = uiState.currentStep)

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content with Crossfade
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(
                    targetState = uiState.currentStep,
                    animationSpec = tween(300),
                    label = "step_crossfade"
                ) { step ->
                    when (step) {
                        1 -> IssueDescriptionStep(
                            description = uiState.issueDescription,
                            selectedCategory = uiState.selectedServiceCategory,
                            onDescriptionChange = viewModel::onDescriptionChange,
                            onCategorySelected = viewModel::onServiceCategorySelected,
                            onNextClick = viewModel::onNextStep
                        )
                        2 -> VendorSelectionStep(
                            vendors = uiState.matchedVendors,
                            isLoading = uiState.isLoadingVendors,
                            selectedVendor = uiState.selectedVendor,
                            onVendorSelected = viewModel::onVendorSelected,
                            onNextClick = viewModel::onNextStep
                        )
                        3 -> SchedulingStep(
                            selectedDate = uiState.selectedDate,
                            selectedTime = uiState.selectedTime,
                            address = uiState.address,
                            onDateSelected = viewModel::onDateSelected,
                            onTimeSelected = viewModel::onTimeSelected,
                            onAddressChange = viewModel::onAddressChange,
                            onNextClick = viewModel::onNextStep
                        )
                        4 -> ConfirmationStep(
                            uiState = uiState,
                            onConfirmClick = viewModel::onConfirmBooking
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================================
// Step Indicator — ServifyBlue active, dark inactive
// ==========================================================
@Composable
fun BookingStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..4) {
            val isActive = i <= currentStep
            val isCurrent = i == currentStep

            val animatedSize by animateDpAsState(
                targetValue = if (isCurrent) 36.dp else 28.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "step_size"
            )

            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> ServifyBlue
                            isActive -> ServifyBlue.copy(alpha = 0.3f)
                            else -> DarkSurfaceLight
                        }
                    )
                    .then(
                        if (isCurrent) Modifier.border(2.dp, ServifyBlue.copy(alpha = 0.5f), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && !isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = ServifyBlue,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = i.toString(),
                        color = if (isActive) Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (i < 4) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    color = if (i < currentStep) ServifyBlue.copy(alpha = 0.4f) else DarkBorder,
                    thickness = 2.dp
                )
            }
        }
    }
}

// ==========================================================
// Step 1: Issue Description
// ==========================================================
@Composable
fun IssueDescriptionStep(
    description: String,
    selectedCategory: String?,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onNextClick: () -> Unit
) {
    val categories = listOf("Home Appliances", "Electronics", "Vehicles", "Electrical", "Plumbing", "Carpentry")

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ServifyBlue,
        unfocusedBorderColor = DarkBorder,
        focusedLabelColor = ServifyBlue,
        unfocusedLabelColor = TextSecondary,
        cursorColor = ServifyBlue,
        focusedContainerColor = DarkSurface,
        unfocusedContainerColor = DarkSurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Describe the Issue",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = Satoshi
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tell us what's wrong and select a service category.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Service Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ServifyBlue.copy(alpha = 0.2f),
                        selectedLabelColor = ServifyBlue,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkBorder,
                        selectedBorderColor = ServifyBlue,
                        enabled = true,
                        selected = selectedCategory == category
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Issue Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(14.dp),
            colors = darkFieldColors
        )

        Spacer(modifier = Modifier.weight(1f))

        ServifyButton(
            text = "Find Vendors",
            onClick = onNextClick,
            enabled = description.isNotBlank() && selectedCategory != null
        )
    }
}

// ==========================================================
// Step 2: Vendor Selection
// ==========================================================
@Composable
fun VendorSelectionStep(
    vendors: List<Vendor>,
    isLoading: Boolean,
    selectedVendor: Vendor?,
    onVendorSelected: (Vendor) -> Unit,
    onNextClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select a Vendor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = Satoshi
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ServifyBlue)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (vendors.isEmpty()) {
                    item {
                        Text(
                            text = "No vendors found for this category.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(vendors) { vendor ->
                        VendorCard(
                            vendor = vendor,
                            isSelected = vendor == selectedVendor,
                            onSelect = { onVendorSelected(vendor) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ServifyButton(
            text = "Continue",
            onClick = onNextClick,
            enabled = selectedVendor != null
        )
    }
}

@Composable
fun VendorCard(
    vendor: Vendor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "vendor_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(1.5.dp, ServifyBlue) else BorderStroke(1.dp, DarkBorder),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ServifyBlue.copy(alpha = 0.08f) else DarkSurface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vendor.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = Satoshi
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${vendor.rating} (${vendor.totalReviews})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "₹${vendor.hourlyRate}/hr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ServifyBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = ServifyBlue)
            }
        }
    }
}

// ==========================================================
// Step 3: Scheduling
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingStep(
    selectedDate: String,
    selectedTime: String,
    address: String,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onNextClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ServifyBlue,
        unfocusedBorderColor = DarkBorder,
        focusedLabelColor = ServifyBlue,
        unfocusedLabelColor = TextSecondary,
        cursorColor = ServifyBlue,
        focusedContainerColor = DarkSurface,
        unfocusedContainerColor = DarkSurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(formatter.format(date))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = ServifyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(formattedTime)
                    showTimePicker = false
                }) {
                    Text("OK", color = ServifyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Schedule Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = Satoshi
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pick a convenient time for the service.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Date Field
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Service Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Event, contentDescription = "Select Date", tint = ServifyBlue)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = darkFieldColors
            )
            Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Field
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { },
                label = { Text("Service Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Select Time", tint = ServifyBlue)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = darkFieldColors
            )
            Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Address Field
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Service Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = darkFieldColors
        )

        Spacer(modifier = Modifier.weight(1f))

        ServifyButton(
            text = "Review & Confirm",
            onClick = onNextClick,
            enabled = selectedDate.isNotBlank() && selectedTime.isNotBlank() && address.isNotBlank()
        )
    }
}

// ==========================================================
// Step 4: Confirmation
// ==========================================================
@Composable
fun ConfirmationStep(
    uiState: CreateBookingUiState,
    onConfirmClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Review Booking",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = Satoshi
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                LabelValueRow("Service", uiState.selectedServiceCategory ?: "N/A")
                LabelValueRow("Vendor", uiState.selectedVendor?.businessName ?: "N/A")
                LabelValueRow("Date", uiState.selectedDate)
                LabelValueRow("Time", uiState.selectedTime)
                LabelValueRow("Address", uiState.address)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DarkBorder
                )
                LabelValueRow("Rate", "₹${uiState.selectedVendor?.hourlyRate ?: 0}/hr", isBold = true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        ServifyButton(
            text = "Confirm Booking",
            onClick = onConfirmClick,
            isLoading = uiState.isCreatingBooking
        )
    }
}

@Composable
fun LabelValueRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
