package com.servify.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }
    
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }
    
    fun onLoginClick() {
        Log.d("LoginViewModel", "Login clicked, email: ${uiState.value.email}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.signIn(uiState.value.email, uiState.value.password)
                .onSuccess { userProfile ->
                    Log.d("LoginViewModel", "Login success for role: ${userProfile.role}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userRole = userProfile.role
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("LoginViewModel", "Login failure", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Login failed"
                        )
                    }
                }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userRole: String? = null,
    val error: String? = null
)
