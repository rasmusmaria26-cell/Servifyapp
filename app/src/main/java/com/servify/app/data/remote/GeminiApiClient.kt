package com.servify.app.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.servify.app.BuildConfig
import com.servify.app.data.model.AIDiagnosis
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiClient @Inject constructor() {

    // Using gemini-pro which is generally available
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
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
                You are an expert technician specializing in $serviceCategory.
                Analyze the following issue description and images (if provided) to provide a preliminary diagnosis.
                
                Issue Description: "$description"
                
                Return ONLY a valid JSON object with the following structure:
                {
                  "diagnosis": "A brief technical diagnosis of the problem",
                  "estimatedCost": "Estimated price range in INR (e.g., ₹500 - ₹1000)",
                  "estimatedTime": "Estimated repair duration (e.g., 1-2 hours)",
                  "recommendedService": "The specific service category required (e.g., AC Repair, Plumbing)",
                  "urgency": "Low, Medium, or High",
                  "possibleCauses": ["List of 2-3 potential causes"]
                }
                
                Do not include any markdown formatting, code blocks, or explanations outside the JSON.
            """.trimIndent()

            val inputContent = content {
                text(prompt)
                images.forEach { image ->
                    image(image)
                }
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.trim() ?: throw Exception("Empty response from AI")
            
            // Clean up potentially formatted response (e.g., ```json ... ```)
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val diagnosis = json.decodeFromString<AIDiagnosis>(cleanJson)
            Result.success(diagnosis)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to Mock Strategy
            Result.success(getMockDiagnosis())
        }
    }

    private fun getMockDiagnosis(): AIDiagnosis {
        return AIDiagnosis(
            diagnosis = "Use Mock: Logic board failure detected in the device.",
            estimatedCost = "₹1200 - ₹2500",
            estimatedTime = "24-48 Hours",
            recommendedService = "Electronics Repair",
            urgency = "Medium",
            possibleCauses = listOf("Power surge", "Water damage", "Component aging")
        )
    }
}
