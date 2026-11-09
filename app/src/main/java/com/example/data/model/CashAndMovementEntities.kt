package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cash register movements:
 * - EXPENSE (مصروفات يومية: فواتير، إيجار، نقل، ضيافة)
 * - DRAWING (سحوبات شخصية: عبدالرحمن)
 * - OTHER_INCOME (إيرادات أخرى: تصوير مستندات، طباعة، خدمات قرطاسية)
 * - SALE_CASH (مبيعات نقدية تلقائية)
 * - PURCHASE_PAYMENT (دفعات موردين)
 */
@Entity(
    tableName = "cash_transactions",
    indices = [
        Index(value = ["type"]),
        Index(value = ["timestamp"])
    ]
)
data class CashTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,               // "EXPENSE", "DRAWING", "OTHER_INCOME", "SALE_CASH", "PURCHASE_PAYMENT"
    val title: String,              // e.g. "فاتورة كهرباء", "سحب شخصي عبدالرحمن", "تصوير مستندات"
    val amount: Double,             // Always positive value
    val isCashIn: Boolean,          // true for inflows (sales, other income), false for outflows (expenses, drawings, supplier payments)
    val category: String = "عام",
    val notes: String = "",
    val referenceId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Audit trail of every stock entry and exit.
 */
@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["timestamp"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val movementType: String,      // "SALE", "SALE_CANCEL", "PURCHASE", "INVENTORY_ADJUSTMENT"
    val quantityChangePieces: Int, // e.g. -12, +24
    val resultingStockPieces: Int,
    val referenceId: String = "",
    val reasonOrNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
