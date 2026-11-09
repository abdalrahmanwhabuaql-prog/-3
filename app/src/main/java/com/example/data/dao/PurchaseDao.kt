package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PurchaseInvoiceEntity
import com.example.data.model.PurchaseItemEntity
import com.example.data.model.SupplierSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchase_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<PurchaseInvoiceEntity>>

    @Query("SELECT * FROM purchase_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): PurchaseInvoiceEntity?

    @Query("SELECT * FROM purchase_items WHERE purchaseInvoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<PurchaseItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: PurchaseInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<PurchaseItemEntity>)

    @Query("UPDATE purchase_invoices SET paidAmount = paidAmount + :payAmount, remainingAmount = remainingAmount - :payAmount WHERE id = :invoiceId")
    suspend fun recordPayment(invoiceId: Long, payAmount: Double)

    @Query("""
        SELECT supplierName, 
               SUM(totalAmount) AS totalPurchased, 
               SUM(paidAmount) AS totalPaid, 
               SUM(remainingAmount) AS totalDebt 
        FROM purchase_invoices 
        GROUP BY supplierName 
        ORDER BY totalDebt DESC
    """)
    fun getSupplierSummaries(): Flow<List<SupplierSummary>>

    @Query("SELECT SUM(remainingAmount) FROM purchase_invoices")
    fun getTotalSupplierDebts(): Flow<Double?>
}
