package com.servify.app.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.servify.app.BuildConfig
import com.servify.app.data.model.AIDiagnosis
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiClient @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
            topP = 0.8f
            maxOutputTokens = 1024
        }
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getDiagnosis(
        description: String,
        images: List<Bitmap> = emptyList(),
        serviceCategory: String = "General Repair"
    ): Result<AIDiagnosis> {
        return try {
            val prompt = """
                You are an expert field technician with 20 years of experience diagnosing and repairing home appliances, electronics, vehicles, plumbing, electrical systems, and carpentry.

                A customer has submitted a repair request through the Servify app. Your job is to analyze their issue description and any photos provided, then return a structured diagnosis that helps them understand the problem and sets accurate expectations before a technician arrives.

                SERVICE CATEGORY: $serviceCategory
                CUSTOMER DESCRIPTION: "$description"

                ANALYSIS INSTRUCTIONS:
                - If photos are provided, examine them carefully for visible damage, wear, corrosion, incorrect installation, or any anomalies
                - Cross-reference what you see in the photos with what the customer described
                - If the description and photos contradict each other, trust the photos
                - Be specific — avoid vague terms like "may need repair". Say exactly what is likely wrong
                - Cost estimates must be realistic for India (INR), accounting for parts and labour
                - Urgency must reflect genuine safety risk: High = safety hazard or will cause further damage if ignored, Medium = should be fixed within a week, Low = cosmetic or convenience issue

                RESPONSE FORMAT:
                Return ONLY a valid JSON object. No markdown, no code blocks, no explanation outside the JSON.

                {
                  "diagnosis": "A clear 1-2 sentence technical diagnosis of what is most likely wrong",
                  "possibleCauses": ["Most likely cause", "Second possible cause", "Third possible cause"],
                  "estimatedCost": "Realistic INR range for parts + labour e.g. ₹800 - ₹1500",
                  "estimatedTime": "Realistic repair duration e.g. 1-2 hours",
                  "recommendedService": "The exact service category needed e.g. AC Repair, Mobile Screen Replacement",
                  "urgency": "Low or Medium or High",
                  "urgencyReason": "One sentence explaining why this urgency level was assigned",
                  "customerAdvice": "One practical thing the customer can do right now before the technician arrives"
                }
            """.trimIndent()

            val inputContent = content {
                text(prompt)
                images.forEach { image(it) }
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.trim()
                ?: throw Exception("Empty response from Gemini")

            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            Result.success(json.decodeFromString<AIDiagnosis>(cleanJson))

        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getMockDiagnosis())
        }
    }

    private fun getMockDiagnosis() = AIDiagnosis(
        diagnosis = "Unable to connect to diagnosis service. A technician will assess on arrival.",
        estimatedCost = "₹500 - ₹2000",
        estimatedTime = "1-3 hours",
        recommendedService = "General Repair",
        urgency = "Medium",
        urgencyReason = "Issue requires professional assessment to determine severity.",
        possibleCauses = listOf("Component failure", "Wear and tear", "Electrical fault"),
        customerAdvice = "Avoid using the appliance until the technician has inspected it."
    )
}
