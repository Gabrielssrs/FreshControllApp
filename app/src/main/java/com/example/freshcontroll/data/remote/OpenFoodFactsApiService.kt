package com.example.freshcontroll.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApiService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OFFResponseDto
}

@JsonClass(generateAdapter = true)
data class OFFResponseDto(
    @Json(name = "code") val code: String,
    @Json(name = "status") val status: Int, // 1 = Encontrado, 0 = No encontrado
    @Json(name = "product") val product: OFFProductDto?
)

@JsonClass(generateAdapter = true)
data class OFFProductDto(
    @Json(name = "product_name") val product_name: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "image_url") val image_url: String?
)