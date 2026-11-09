package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_invoices",
    indices = [
        Index(value = ["invoiceNumber"]),
        Index(value = ["supplierName"]),
        Index(value = ["timestamp"])
    ]
)
data class PurchaseInvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val supplierName: String,
    val supplierPhone: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    indices = [
        Index(value = ["purchaseInvoiceId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseInvoiceId: Long,
    val productId: Long,
    val productName: String,
    val unitPurchased: String,    // "كرتونة", "دستة", "باكيت", "قطعة"
    val quantityUnits: Int,
    val conversionFactor: Int,    // Pieces per purchase unit
    val totalPiecesAdded: Int,    // quantityUnits * conversionFactor
    val unitCost: Double,
    val totalCost: Double
)
