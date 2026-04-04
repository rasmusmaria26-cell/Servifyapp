package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.ServifyButton

data class DeviceOption(val name: String, val emoji: String, val tint: Color)

val devices = listOf(
    DeviceOption("Smartphone",     "📱", Color(0xFFE6F1FB)),
    DeviceOption("Laptop",         "💻", Color(0xFFE6F1FB)),
    DeviceOption("AC",             "❄️", Color(0xFFFAEEDA)),
    DeviceOption("Washing Machine","🫧", Color(0xFFFAEEDA)),
    DeviceOption("Refrigerator",   "🧊", Color(0xFFE1F5EE)),
    DeviceOption("TV",             "📺", Color(0xFFE6F1FB)),
    DeviceOption("Plumbing",       "🔧", Color(0xFFE1F5EE)),
    DeviceOption("Electrical",     "⚡", Color(0xFFFAEEDA)),
    DeviceOption("Other",          "➕", Color(0xFFF1EFE8)),
)

@Composable
fun StepOneContent(
    selectedDeviceType: String,
    onDeviceTypeSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What needs to be fixed?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(devices) { device ->
                val isSelected = selectedDeviceType == device.name
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFE6F1FB) else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFF378ADD) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onDeviceTypeSelected(device.name) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(device.tint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = device.emoji, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = device.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ServifyButton(
            text = "Next →",
            onClick = onNext,
            enabled = selectedDeviceType.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
