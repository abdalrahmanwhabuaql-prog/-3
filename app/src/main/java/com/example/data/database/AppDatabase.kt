package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CashDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.dao.SaleDao
import com.example.data.dao.StockMovementDao
import com.example.data.model.CashTransactionEntity
import com.example.data.model.ProductEntity
import com.example.data.model.PurchaseInvoiceEntity
import com.example.data.model.PurchaseItemEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StockMovementEntity

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseInvoiceEntity::class,
        PurchaseItemEntity::class,
        CashTransactionEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun cashDao(): CashDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_aqsa_pos.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
