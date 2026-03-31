package com.servify.app.presentation.customer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.data.model.RepairRequest
import com.servify.app.data.repository.AuthRepository
import com.servify.app.data.repository.RepairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import javax.inject.Inject

data class PostRepairUiState(
    // Form fields
    val deviceType: String = "",
    val brand: String = "",
    val model: String = "",
    val issueCategory: String = "",
    val severity: String = "MODERATE",
    val description: String = "",
    val mediaUris: List<Uri> = emptyList(),

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",

    // Submission state
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submittedRequestId: String? = null   // non-null → success
)

@HiltViewModel
class PostRepairRequestViewModel @Inject constructor(
    private val repairRepository: RepairRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostRepairUiState())
    val uiState: StateFlow<PostRepairUiState> = _uiState.asStateFlow()

    // ── Form field mutators ───────────────────────────────────────────────

    fun onDeviceType(v: String)     = _uiState.update { it.copy(deviceType = v) }
    fun onBrand(v: String)          = _uiState.update { it.copy(brand = v) }
    fun onModel(v: String)          = _uiState.update { it.copy(model = v) }
    fun onIssueCategory(v: String)  = _uiState.update { it.copy(issueCategory = v) }
    fun onSeverity(v: String)       = _uiState.update { it.copy(severity = v) }
    fun onDescription(v: String)    = _uiState.update { it.copy(description = v) }
    fun onAddress(v: String)        = _uiState.update { it.copy(address = v) }
    fun onLocationSelected(lat: Double, lng: Double) = _uiState.update { 
        it.copy(latitude = lat, longitude = lng) 
    }

    fun addMediaUri(uri: Uri) = _uiState.update {
        it.copy(mediaUris = it.mediaUris + uri)
    }

    fun removeMediaUri(uri: Uri) = _uiState.update {
        it.copy(mediaUris = it.mediaUris - uri)
    }

    // ── Submission ────────────────────────────────────────────────────────

    fun submit() {
        val s = _uiState.value
        if (s.deviceType.isBlank() || s.issueCategory.isBlank() || s.description.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            // Resolve the current user id (auth UUID — must match auth.uid() for RLS)
            val userId = authRepository.getCurrentUser()?.userId
            if (userId == null) {
                _uiState.update { it.copy(isSubmitting = false, error = "Not logged in.") }
                return@launch
            }

            // Compute the 60-minute bidding deadline (ISO-8601 UTC)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val deadline = sdf.format(java.util.Date(System.currentTimeMillis() + 60 * 60 * 1000L))

            val request = RepairRequest(
                customerId    = userId,
                deviceType    = s.deviceType,
                brand         = s.brand,
                model         = s.model,
                issueCategory = s.issueCategory,
                severity      = s.severity,
                description   = s.description,
                quoteDeadline = deadline,
                latitude      = s.latitude ?: 28.6139,  // default if map unchanged
                longitude     = s.longitude ?: 77.2090,
                address       = s.address
                // mediaUrls populated separately after image upload
            )

            repairRepository.createRepairRequest(request)
                .onSuccess { created ->
                    _uiState.update { it.copy(isSubmitting = false, submittedRequestId = created.id) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Submission failed.") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
