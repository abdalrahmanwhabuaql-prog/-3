package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Product in Al-Aqsa Stationery store.
 * Supports dual purchase/sale units:
 * e.g., bought as a carton of 24 pieces or a dozen of 12 pieces,
 * and sold either as single pieces or entire dozens/cartons.
 * Stock is maintained internally in base pieces for 100% exact math.
 */
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["category"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String = "",
    val category: String = "قرطاسية عامة",
    // Unit definitions
    val purchaseUnit: String = "كرتونة", // كرتونة، دستة، باكيت، قطعة
    val conversionFactor: Int = 12,       // Number of base pieces inside 1 purchase unit (e.g. 12 or 24)
    val purchaseCostPerUnit: Double = 0.0, // Cost paid for 1 purchase unit (e.g., 24.00 for dozen)
    val costPerPiece: Double = 0.0,        // purchaseCostPerUnit / conversionFactor (e.g. 2.00)
    val saleUnit: String = "قطعة",        // قطعة، دستة، كرتونة
    val salePricePerPiece: Double = 0.0,   // Price to sell 1 single piece (e.g. 3.00)
    val salePricePerDozen: Double = 0.0,   // Optional discounted price if sold by dozen (e.g. 32.00)
    val salePricePerCarton: Double = 0.0,  // Optional discounted price if sold by carton
    val stockPieces: Int = 0,              // Total base pieces in inventory
    val minStockAlert: Int = 5,            // Threshold in pieces to trigger low stock warning
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to format available stock in readable Arabic
    fun formattedStock(): String {
        return when {
            conversionFactor > 1 && stockPieces >= conversionFactor -> {
                val fullUnits = stockPieces / conversionFactor
                val remainingPieces = stockPieces % conversionFactor
                if (remainingPieces == 0) {
                    "$fullUnits $purchaseUnit ($stockPieces قطعة)"
                } else {
                    "$fullUnits $purchaseUnit و $remainingPieces قطعة ($stockPieces قطعة)"
                }
            }
            else -> "$stockPieces قطعة"
        }
    }

    val isLowStock: Boolean
        get() = stockPieces <= minStockAlert
}
