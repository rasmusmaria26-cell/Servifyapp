package com.servify.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClientProvider {
    
    private const val SUPABASE_URL = "https://cfawrcxomfpfpzfndrtj.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmYXdyY3hvbWZwZnB6Zm5kcnRqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ3NzkwOTAsImV4cCI6MjA4MDM1NTA5MH0.M_mpoCNPOzGDHNt_uYLwtpzwnMwb6dklAyWJPZswROg"
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
            
            // Fix for JSON serialization errors when new fields are added to DB
            defaultSerializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                    coerceInputValues = true
                }
            )
        }
    }
}
