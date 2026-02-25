package com.servify.app.data.repository

import android.util.Log
import com.servify.app.data.model.Vendor
import com.servify.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
    private val supabase: io.github.jan.supabase.SupabaseClient
) {

    suspend fun getVendors(categoryName: String): Result<List<Vendor>> = withContext(Dispatchers.IO) {
        try {
            Log.d("VendorRepository", "Fetching vendors for category: $categoryName")
            
            // 1. If "General", return all verified vendors
            if (categoryName.equals("General", ignoreCase = true)) {
                 val vendors = supabase.from("vendors")
                    .select {
                        filter {
                            eq("is_verified", true)
                        }
                    }
                    .decodeList<Vendor>()
                 Log.d("VendorRepository", "General query returned ${vendors.size} vendors")
                 return@withContext Result.success(vendors)
            }

            // 2. Resolve Category Name to ID
            val categoryId = try {
                val category = supabase.from("service_categories")
                    .select {
                        filter {
                            eq("name", categoryName) 
                        }
                    }
                    .decodeSingleOrNull<com.servify.app.data.model.ServiceCategory>()
                category?.id
            } catch (e: Exception) {
                Log.e("VendorRepository", "Failed to resolve category name: $categoryName", e)
                null
            }

            if (categoryId == null) {
                Log.w("VendorRepository", "Category '$categoryName' not found in DB or resolution failed")
                return@withContext Result.success(emptyList()) 
            }

            Log.d("VendorRepository", "Resolved '$categoryName' to ID: $categoryId")

            // 3. Fetch Vendors using native array filtering
            val filteredVendors = supabase.from("vendors")
                .select {
                    filter {
                        eq("is_verified", true)
                        // Use contains filter for the service_categories array
                        // Postgrest syntax: service_categories=cs.{uuid}
                        contains("service_categories", listOf(categoryId))
                    }
                }
                .decodeList<Vendor>()

            Log.d("VendorRepository", "Query returned ${filteredVendors.size} vendors for $categoryName ($categoryId)")
            
            Result.success(filteredVendors)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to fetch vendors", e)
            Result.failure(e)
        }
    }

    suspend fun getVendorByUserId(userId: String): Result<Vendor?> = withContext(Dispatchers.IO) {
        try {
            Log.d("VendorRepository", "Fetching vendor for user: $userId")
            val vendor = supabase.from("vendors")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<Vendor>()
            
            Log.d("VendorRepository", "Vendor fetch result: ${vendor?.businessName}")
            Result.success(vendor)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to fetch vendor for user: $userId", e)
            Result.failure(e)
        }
    }
}
