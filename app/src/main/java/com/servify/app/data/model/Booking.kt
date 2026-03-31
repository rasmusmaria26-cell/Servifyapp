package com.servify.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.servify.app.data.model.Service
import com.servify.app.data.model.ProfileDto
import com.servify.app.data.model.Vendor

@Serializable
data class Booking(
    val id: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("vendor_id")
    val vendorId: String?,
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("services")
    val service: Service? = null,
    @SerialName("issue_description")
    val issueDescription: String,
    @SerialName("ai_diagnosis")
    val aiDiagnosis: AIDiagnosis?,
    @SerialName("scheduled_date")
    val scheduledDate: String,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("estimated_cost")
    val estimatedPrice: Double?,
    @SerialName("final_cost")
    val finalPrice: Double?,
    val status: String, // PENDING, ACCEPTED, etc.
    @SerialName("payment_status")
    val paymentStatus: String, // PENDING, PAID
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("vendor")
    val vendorProfile: ProfileDto? = null,
    @SerialName("vendor_details")
    val vendorDetails: Vendor? = null
)

@Serializable
data class AIDiagnosis(
    val diagnosis: String,
    val estimatedCost: String,
    val estimatedTime: String,
    val recommendedService: String,
    val urgency: String,
    val urgencyReason: String = "",
    val possibleCauses: List<String>,
    val customerAdvice: String = ""
)
