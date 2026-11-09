package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val totalCost: Double = 0.0,
    val grossProfit: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH (نقدي) or CREDIT (آجل)
    val customerName: String = "",       // Optional for credit sales
    val isCancelled: Boolean = false,
    val cancellationReason: String? = null,
    val cancelledAt: Long? = null
)

@Entity(
    tableName = "sale_items",
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unitSold: String,         // "قطعة", "دستة", "كرتونة"
    val piecesPerUnit: Int,       // 1 for piece, 12 for dozen, etc.
    val quantityUnits: Double,    // e.g. 2 pieces or 1 dozen
    val totalPieces: Int,         // quantityUnits * piecesPerUnit
    val unitPrice: Double,        // price charged per unit
    val unitCost: Double,         // cost of goods per unit
    val totalPrice: Double,       // total charged for this line
    val totalCost: Double         // total cost for this line
)
