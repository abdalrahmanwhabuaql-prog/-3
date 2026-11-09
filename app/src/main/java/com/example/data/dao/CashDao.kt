package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CashTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CashTransactionEntity>>

    @Query("SELECT * FROM cash_transactions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getTransactionsSince(sinceTimestamp: Long): Flow<List<CashTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CashTransactionEntity): Long

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE isCashIn = 1")
    fun getTotalCashIn(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE isCashIn = 0")
    fun getTotalCashOut(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE type = 'EXPENSE' AND timestamp >= :sinceTimestamp")
    fun getExpensesSince(sinceTimestamp: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE type = 'DRAWING' AND timestamp >= :sinceTimestamp")
    fun getDrawingsSince(sinceTimestamp: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE type = 'OTHER_INCOME' AND timestamp >= :sinceTimestamp")
    fun getOtherIncomeSince(sinceTimestamp: Long): Flow<Double?>
}
