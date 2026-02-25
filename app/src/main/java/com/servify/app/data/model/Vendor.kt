package com.servify.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    val id: String,
    @SerialName("business_name")
    val businessName: String,
    @SerialName("service_categories")
    val serviceCategories: List<String>,
    @SerialName("hourly_rate")
    val hourlyRate: Double,
    val rating: Double,
    @SerialName("total_reviews")
    val totalReviews: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("service_radius_km")
    val serviceRadiusKm: Double,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("is_available")
    val isAvailable: Boolean,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    @SerialName("kyc_status")
    val kycStatus: String? = null,
    @SerialName("total_jobs")
    val totalJobs: Int? = 0,
    val phone: String? = null
)
