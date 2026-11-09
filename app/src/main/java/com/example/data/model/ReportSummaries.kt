package com.example.data.model

data class TopProductSummary(
    val productId: Long,
    val productName: String,
    val totalPiecesSold: Int,
    val totalRevenue: Double
)

data class SupplierSummary(
    val supplierName: String,
    val totalPurchased: Double,
    val totalPaid: Double,
    val totalDebt: Double
)
