package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.TopProductSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getSalesSince(sinceTimestamp: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleById(saleId: Long): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSaleFlow(saleId: Long): Flow<List<SaleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("UPDATE sales SET isCancelled = 1, cancellationReason = :reason, cancelledAt = :cancelledAt WHERE id = :saleId")
    suspend fun cancelSale(saleId: Long, reason: String, cancelledAt: Long)

    @Query("""
        SELECT productId, productName, SUM(totalPieces) AS totalPiecesSold, SUM(totalPrice) AS totalRevenue
        FROM sale_items
        INNER JOIN sales ON sale_items.saleId = sales.id
        WHERE sales.isCancelled = 0 AND sales.timestamp >= :sinceTimestamp
        GROUP BY productId, productName
        ORDER BY totalPiecesSold DESC
        LIMIT :limit
    """)
    fun getTopSellingProducts(sinceTimestamp: Long = 0, limit: Int = 10): Flow<List<TopProductSummary>>

    @Query("""
        SELECT DISTINCT productId FROM sale_items
        INNER JOIN sales ON sale_items.saleId = sales.id
        WHERE sales.isCancelled = 0 AND sales.timestamp >= :sinceTimestamp
    """)
    suspend fun getSoldProductIdsSince(sinceTimestamp: Long): List<Long>
}
