package com.example.freshcontroll.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenFoodFactsResponseDto(
    @Json(name = "status") val status: Int,          // 1 = encontrado, 0 = no encontrado
    @Json(name = "status_verbose") val statusVerbose: String?,
    @Json(name = "product") val product: OpenFoodFactsProductDto?
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductDto(
    @Json(name = "code") val code: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "image_front_url") val imageUrl: String?,
    @Json(name = "quantity") val quantity: String?
)
