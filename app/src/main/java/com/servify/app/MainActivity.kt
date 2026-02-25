package com.servify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.servify.app.presentation.customer.CreateBookingScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.servify.app.presentation.auth.LoginScreen
import com.servify.app.presentation.home.HomeScreen
import com.servify.app.presentation.splash.SplashScreen
import com.servify.app.presentation.vendor.VendorDashboardScreen
import com.servify.app.ui.theme.ServifyTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ServifyTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToHome = { role ->
                                    navController.navigate("home/$role") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onNavigateToSignup = {
                                    navController.navigate("signup")
                                },
                                onLoginSuccess = { role ->
                                    navController.navigate("home/$role") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("signup") {
                            com.servify.app.presentation.auth.SignupScreen(
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                },
                                onSignupSuccess = { role ->
                                    navController.navigate("home/$role") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("home/{role}") { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "User"
                            if (role.equals("customer", ignoreCase = true)) {
                                com.servify.app.presentation.customer.CustomerDashboardScreen(
                                    onNavigateToBooking = {
                                        navController.navigate("create_booking")
                                    },
                                    onNavigateToBookingDetail = { bookingId ->
                                        navController.navigate("booking_detail/$bookingId")
                                    },
                                    onSignOut = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            } else if (role.equals("vendor", ignoreCase = true)) {
                                VendorDashboardScreen()
                            } else {
                                HomeScreen(role = role)
                            }
                        }

                        composable("create_booking") {
                            CreateBookingScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onBookingCreated = { 
                                    navController.popBackStack() 
                                }
                            )
                        }

                        composable("booking_detail/{bookingId}") { backStackEntry ->
                            // Get the dashboard ViewModel from the parent backstack entry
                            val parentEntry = remember(backStackEntry) {
                                navController.getBackStackEntry("home/{role}")
                            }
                            val dashboardViewModel: com.servify.app.presentation.customer.CustomerDashboardViewModel = 
                                hiltViewModel(parentEntry)
                            val booking = dashboardViewModel.selectedBooking
                            if (booking != null) {
                                com.servify.app.presentation.customer.BookingDetailScreen(
                                    booking = booking,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}