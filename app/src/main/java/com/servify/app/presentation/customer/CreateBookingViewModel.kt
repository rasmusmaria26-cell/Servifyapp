package com.servify.app.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.data.model.Booking
import com.servify.app.data.model.Vendor
import com.servify.app.domain.usecase.booking.CreateBookingUseCase
import com.servify.app.domain.usecase.vendor.GetMatchedVendorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateBookingViewModel @Inject constructor(
    private val getMatchedVendorsUseCase: GetMatchedVendorsUseCase,
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBookingUiState())
    val uiState: StateFlow<CreateBookingUiState> = _uiState.asStateFlow()


    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(issueDescription = description) }
    }

    fun onServiceCategorySelected(category: String) {
        _uiState.update { it.copy(selectedServiceCategory = category) }
    }

    fun onVendorSelected(vendor: Vendor) {
        _uiState.update { it.copy(selectedVendor = vendor) }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onNextStep() {
        val nextStep = _uiState.value.currentStep + 1
        _uiState.update { it.copy(currentStep = nextStep) }
        
        if (nextStep == 2) {
            fetchVendors()
        }
    }

    private fun fetchVendors() {
        viewModelScope.launch {
            val category = _uiState.value.selectedServiceCategory ?: "Electronics"
            _uiState.update { it.copy(isLoadingVendors = true, error = null) }
            
            getMatchedVendorsUseCase(category)
                .onSuccess { vendors ->
                    _uiState.update { 
                        it.copy(
                            isLoadingVendors = false, 
                            matchedVendors = vendors
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoadingVendors = false, 
                            error = "Failed to load vendors: ${error.message}"
                        ) 
                    }
                }
        }
    }

    fun onConfirmBooking() {
        val currentState = _uiState.value
        if (currentState.isCreatingBooking || currentState.bookingCreated) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, error = null) }
            
            createBookingUseCase(
                serviceCategory = currentState.selectedServiceCategory ?: "AC Repair",
                issueDescription = currentState.issueDescription,
                aiDiagnosis = null,
                scheduledDate = currentState.selectedDate,
                scheduledTime = currentState.selectedTime,
                address = currentState.address,
                vendorId = currentState.selectedVendor?.id,
                estimatedPrice = null // No AI to estimate cost
            ).onSuccess { booking ->
                _uiState.update { 
                    it.copy(
                        isCreatingBooking = false, 
                        bookingCreated = true 
                    ) 
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isCreatingBooking = false, 
                        error = "Booking failed: ${error.message}"
                    ) 
                }
            }
        }
    }
}

data class CreateBookingUiState(
    val currentStep: Int = 1,
    val issueDescription: String = "",
    val selectedServiceCategory: String? = null,
    val selectedImages: List<String> = emptyList(),
    
    val isLoadingVendors: Boolean = false,
    val matchedVendors: List<Vendor> = emptyList(),
    val selectedVendor: Vendor? = null,
    
    val selectedDate: String = "",
    val selectedTime: String = "",
    val address: String = "",
    
    val isCreatingBooking: Boolean = false,
    val bookingCreated: Boolean = false,
    val error: String? = null
)
