package com.servify.app.feature.customer.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servify.app.designsystem.ServifyButton
import androidx.compose.ui.text.font.FontWeight

val ISSUE_CATEGORIES_LIST = listOf(
    "Screen / Display", "Battery", "Charging Port",
    "Software / OS Crash", "Water Damage", "Speaker / Mic",
    "Keyboard / Trackpad", "Cooling / Fan", "Power (Won't turn on)",
    "Camera", "Other"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepTwoContent(
    selectedDeviceType: String,
    selectedIssueCategory: String,
    onIssueCategorySelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Green chip showing completed step 1 answer
        AssistChip(
            onClick = onBack,
            label = { Text(selectedDeviceType, fontSize = 10.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color(0xFFEAF3DE),
                labelColor = Color(0xFF3B6D11)
            ),
            border = BorderStroke(1.dp, Color(0xFFC0DD97))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "What's the main issue?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ISSUE_CATEGORIES_LIST.forEach { category ->
                val isSelected = selectedIssueCategory == category
                SuggestionChip(
                    onClick = { onIssueCategorySelected(category) },
                    label = { Text(category, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) Color(0xFF378ADD) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ServifyButton(
            text = "Next →",
            onClick = onNext,
            enabled = selectedIssueCategory.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }
    }
}
