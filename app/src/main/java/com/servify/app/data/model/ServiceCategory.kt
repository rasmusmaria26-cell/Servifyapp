package com.servify.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceCategory(
    val id: String,
    val name: String,
    val slug: String? = null
)
