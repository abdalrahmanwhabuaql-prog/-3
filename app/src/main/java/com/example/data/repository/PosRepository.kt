package com.example.data.repository

import com.example.data.dao.CashDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.dao.SaleDao
import com.example.data.dao.StockMovementDao
import com.example.data.model.SupplierSummary
import com.example.data.model.TopProductSummary
import com.example.data.database.AppDatabase
import com.example.data.database.SampleData
import com.example.data.model.CashTransactionEntity
import com.example.data.model.ProductEntity
import com.example.data.model.PurchaseInvoiceEntity
import com.example.data.model.PurchaseItemEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: ProductEntity,
    val unitType: String = "قطعة", // "قطعة", "دستة", "كرتونة"
    val piecesPerUnit: Int = 1,
    val quantityUnits: Double = 1.0,
    val unitPrice: Double = product.salePricePerPiece,
    val unitCost: Double = product.costPerPiece
) {
    val totalPieces: Int
        get() = (quantityUnits * piecesPerUnit).toInt()

    val lineTotal: Double
        get() = quantityUnits * unitPrice

    val lineCost: Double
        get() = quantityUnits * unitCost
}

class PosRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val purchaseDao = db.purchaseDao()
    private val cashDao = db.cashDao()
    private val movementDao = db.stockMovementDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allPurchases: Flow<List<PurchaseInvoiceEntity>> = purchaseDao.getAllInvoices()
    val supplierSummaries: Flow<List<SupplierSummary>> = purchaseDao.getSupplierSummaries()
    val allCashTransactions: Flow<List<CashTransactionEntity>> = cashDao.getAllTransactions()
    val allStockMovements: Flow<List<StockMovementEntity>> = movementDao.getAllMovements()
    val totalInventoryCost: Flow<Double?> = productDao.getTotalInventoryCost()
    val totalSupplierDebts: Flow<Double?> = purchaseDao.getTotalSupplierDebts()

    suspend fun initSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        if (productDao.getProductCount() == 0) {
            val sampleItems = SampleData.getInitialProducts()
            productDao.insertAll(sampleItems)
            // Log initial stock movements
            val movements = sampleItems.map { item ->
                StockMovementEntity(
                    productId = item.id,
                    productName = item.name,
                    movementType = "INVENTORY_INIT",
                    quantityChangePieces = item.stockPieces,
                    resultingStockPieces = item.stockPieces,
                    referenceId = "INIT-STOCK",
                    reasonOrNotes = "الرصيد الافتتاحي للمخزون"
                )
            }
            movementDao.insertAll(movements)
        }
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByBarcode(barcode.trim())
    }

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val calculatedCostPerPiece = if (product.conversionFactor > 0) {
            product.purchaseCostPerUnit / product.conversionFactor
        } else product.purchaseCostPerUnit

        val finalProduct = product.copy(costPerPiece = calculatedCostPerPiece)
        if (finalProduct.id == 0L) {
            val id = productDao.insertProduct(finalProduct)
            movementDao.insertMovement(
                StockMovementEntity(
                    productId = id,
                    productName = finalProduct.name,
                    movementType = "INVENTORY_ADJUSTMENT",
                    quantityChangePieces = finalProduct.stockPieces,
                    resultingStockPieces = finalProduct.stockPieces,
                    referenceId = "NEW-PROD",
                    reasonOrNotes = "إضافة منتج جديد للمكتبة"
                )
            )
            id
        } else {
            val old = productDao.getProductById(finalProduct.id)
            productDao.updateProduct(finalProduct)
            if (old != null && old.stockPieces != finalProduct.stockPieces) {
                val diff = finalProduct.stockPieces - old.stockPieces
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = finalProduct.id,
                        productName = finalProduct.name,
                        movementType = "INVENTORY_ADJUSTMENT",
                        quantityChangePieces = diff,
                        resultingStockPieces = finalProduct.stockPieces,
                        referenceId = "EDIT-PROD",
                        reasonOrNotes = "تعديل كمية المخزون يدوياً"
                    )
                )
            }
            finalProduct.id
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun processSale(
        items: List<CartItem>,
        paymentMethod: String,
        customerName: String
    ): Long = withContext(Dispatchers.IO) {
        val totalAmount = items.sumOf { it.lineTotal }
        val totalCost = items.sumOf { it.lineCost }
        val grossProfit = totalAmount - totalCost
        val invoiceNumber = "INV-" + SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())

        val saleEntity = SaleEntity(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            totalCost = totalCost,
            grossProfit = grossProfit,
            paymentMethod = paymentMethod,
            customerName = customerName
        )
        val saleId = saleDao.insertSale(saleEntity)

        val saleItems = items.map { item ->
            SaleItemEntity(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                unitSold = item.unitType,
                piecesPerUnit = item.piecesPerUnit,
                quantityUnits = item.quantityUnits,
                totalPieces = item.totalPieces,
                unitPrice = item.unitPrice,
                unitCost = item.unitCost,
                totalPrice = item.lineTotal,
                totalCost = item.lineCost
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Deduct inventory & record movement
        items.forEach { cartItem ->
            val deductPieces = cartItem.totalPieces
            productDao.adjustStock(cartItem.product.id, -deductPieces)
            val updated = productDao.getProductById(cartItem.product.id)
            val remaining = updated?.stockPieces ?: 0

            movementDao.insertMovement(
                StockMovementEntity(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    movementType = "SALE",
                    quantityChangePieces = -deductPieces,
                    resultingStockPieces = remaining,
                    referenceId = invoiceNumber,
                    reasonOrNotes = "عملية بيع كاشير ($invoiceNumber)"
                )
            )
        }

        // Record Cash Transaction if Cash
        if (paymentMethod.equals("CASH", ignoreCase = true)) {
            cashDao.insertTransaction(
                CashTransactionEntity(
                    type = "SALE_CASH",
                    title = "مبيعات نقدية فاتورة $invoiceNumber",
                    amount = totalAmount,
                    isCashIn = true,
                    category = "مبيعات",
                    referenceId = invoiceNumber
                )
            )
        }

        saleId
    }

    suspend fun cancelSale(saleId: Long, reason: String) = withContext(Dispatchers.IO) {
        val sale = saleDao.getSaleById(saleId) ?: return@withContext
        if (sale.isCancelled) return@withContext

        val items = saleDao.getItemsForSale(saleId)
        saleDao.cancelSale(saleId, reason, System.currentTimeMillis())

        // Replenish stock
        items.forEach { item ->
            productDao.adjustStock(item.productId, item.totalPieces)
            val updated = productDao.getProductById(item.productId)
            val currentStock = updated?.stockPieces ?: 0

            movementDao.insertMovement(
                StockMovementEntity(
                    productId = item.productId,
                    productName = item.productName,
                    movementType = "SALE_CANCEL",
                    quantityChangePieces = item.totalPieces,
                    resultingStockPieces = currentStock,
                    referenceId = sale.invoiceNumber,
                    reasonOrNotes = "إلغاء بيع ($reason)"
                )
            )
        }

        // Deduct cash if it was cash
        if (sale.paymentMethod.equals("CASH", ignoreCase = true)) {
            cashDao.insertTransaction(
                CashTransactionEntity(
                    type = "SALE_CANCEL",
                    title = "إلغاء مبيعات فاتورة ${sale.invoiceNumber}",
                    amount = sale.totalAmount,
                    isCashIn = false,
                    category = "إلغاءات",
                    notes = reason,
                    referenceId = sale.invoiceNumber
                )
            )
        }
    }

    suspend fun getSaleWithItems(saleId: Long): Pair<SaleEntity?, List<SaleItemEntity>> = withContext(Dispatchers.IO) {
        val sale = saleDao.getSaleById(saleId)
        val items = saleDao.getItemsForSale(saleId)
        Pair(sale, items)
    }

    suspend fun recordPurchaseInvoice(
        supplierName: String,
        supplierPhone: String,
        paidAmount: Double,
        notes: String,
        items: List<PurchaseItemInput>
    ): Long = withContext(Dispatchers.IO) {
        val totalAmount = items.sumOf { it.totalCost }
        val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
        val invoiceNumber = "PUR-" + SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())

        val invoice = PurchaseInvoiceEntity(
            invoiceNumber = invoiceNumber,
            supplierName = supplierName.trim(),
            supplierPhone = supplierPhone.trim(),
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            notes = notes.trim()
        )
        val invoiceId = purchaseDao.insertInvoice(invoice)

        val purchaseItems = items.map { input ->
            val totalPieces = input.quantityUnits * input.conversionFactor
            PurchaseItemEntity(
                purchaseInvoiceId = invoiceId,
                productId = input.productId,
                productName = input.productName,
                unitPurchased = input.unitPurchased,
                quantityUnits = input.quantityUnits,
                conversionFactor = input.conversionFactor,
                totalPiecesAdded = totalPieces,
                unitCost = input.unitCost,
                totalCost = input.totalCost
            )
        }
        purchaseDao.insertInvoiceItems(purchaseItems)

        // Increase inventory for each product
        items.forEach { input ->
            val piecesToAdd = input.quantityUnits * input.conversionFactor
            productDao.adjustStock(input.productId, piecesToAdd)
            val updated = productDao.getProductById(input.productId)
            val newStock = updated?.stockPieces ?: 0

            // Also update product purchase price and cost per piece if desired
            if (updated != null && input.conversionFactor > 0) {
                val newPieceCost = input.unitCost / input.conversionFactor
                productDao.updateProduct(
                    updated.copy(
                        purchaseCostPerUnit = input.unitCost,
                        conversionFactor = input.conversionFactor,
                        purchaseUnit = input.unitPurchased,
                        costPerPiece = newPieceCost
                    )
                )
            }

            movementDao.insertMovement(
                StockMovementEntity(
                    productId = input.productId,
                    productName = input.productName,
                    movementType = "PURCHASE",
                    quantityChangePieces = piecesToAdd,
                    resultingStockPieces = newStock,
                    referenceId = invoiceNumber,
                    reasonOrNotes = "شراء بضاعة من المورد $supplierName ($invoiceNumber)"
                )
            )
        }

        // Cash outflow if paid
        if (paidAmount > 0) {
            cashDao.insertTransaction(
                CashTransactionEntity(
                    type = "PURCHASE_PAYMENT",
                    title = "دفعة للمورد $supplierName فاتورة $invoiceNumber",
                    amount = paidAmount,
                    isCashIn = false,
                    category = "مشتريات وبضاعة",
                    referenceId = invoiceNumber
                )
            )
        }

        invoiceId
    }

    suspend fun paySupplierDebt(invoiceId: Long, supplierName: String, payAmount: Double) = withContext(Dispatchers.IO) {
        purchaseDao.recordPayment(invoiceId, payAmount)
        cashDao.insertTransaction(
            CashTransactionEntity(
                type = "PURCHASE_PAYMENT",
                title = "سداد دين للمورد $supplierName",
                amount = payAmount,
                isCashIn = false,
                category = "سداد ذمم موردين",
                referenceId = "PAY-$invoiceId"
            )
        )
    }

    suspend fun addCashTransaction(
        type: String, // "EXPENSE", "DRAWING", "OTHER_INCOME"
        title: String,
        amount: Double,
        category: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val isCashIn = type == "OTHER_INCOME"
        cashDao.insertTransaction(
            CashTransactionEntity(
                type = type,
                title = title.trim(),
                amount = amount,
                isCashIn = isCashIn,
                category = category.trim(),
                notes = notes.trim()
            )
        )
    }

    fun getTopProducts(since: Long = 0): Flow<List<TopProductSummary>> = saleDao.getTopSellingProducts(since)

    fun getSalesSince(since: Long): Flow<List<SaleEntity>> = saleDao.getSalesSince(since)

    suspend fun getStagnantProducts(sinceTimestamp: Long): List<ProductEntity> = withContext(Dispatchers.IO) {
        val all = productDao.getAllProducts().first()
        val soldIds = saleDao.getSoldProductIdsSince(sinceTimestamp).toSet()
        all.filter { it.id !in soldIds }
    }

    // JSON Backup Export
    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTime", System.currentTimeMillis())
        root.put("storeName", "مكتبة الأقصى – عبدالرحمن")

        val productsArray = JSONArray()
        productDao.getAllProducts().first().forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("barcode", p.barcode)
            obj.put("category", p.category)
            obj.put("purchaseUnit", p.purchaseUnit)
            obj.put("conversionFactor", p.conversionFactor)
            obj.put("purchaseCostPerUnit", p.purchaseCostPerUnit)
            obj.put("costPerPiece", p.costPerPiece)
            obj.put("saleUnit", p.saleUnit)
            obj.put("salePricePerPiece", p.salePricePerPiece)
            obj.put("salePricePerDozen", p.salePricePerDozen)
            obj.put("salePricePerCarton", p.salePricePerCarton)
            obj.put("stockPieces", p.stockPieces)
            obj.put("minStockAlert", p.minStockAlert)
            productsArray.put(obj)
        }
        root.put("products", productsArray)

        val salesArray = JSONArray()
        saleDao.getAllSales().first().forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("invoiceNumber", s.invoiceNumber)
            obj.put("timestamp", s.timestamp)
            obj.put("totalAmount", s.totalAmount)
            obj.put("totalCost", s.totalCost)
            obj.put("grossProfit", s.grossProfit)
            obj.put("paymentMethod", s.paymentMethod)
            obj.put("customerName", s.customerName)
            obj.put("isCancelled", s.isCancelled)
            obj.put("cancellationReason", s.cancellationReason ?: "")
            salesArray.put(obj)
        }
        root.put("sales", salesArray)

        val cashArray = JSONArray()
        cashDao.getAllTransactions().first().forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("type", c.type)
            obj.put("title", c.title)
            obj.put("amount", c.amount)
            obj.put("isCashIn", c.isCashIn)
            obj.put("category", c.category)
            obj.put("notes", c.notes)
            obj.put("timestamp", c.timestamp)
            cashArray.put(obj)
        }
        root.put("cashTransactions", cashArray)

        root.toString(2)
    }

    // JSON Backup Import
    suspend fun importDatabaseFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val productsArray = root.optJSONArray("products") ?: JSONArray()
            val importedList = mutableListOf<ProductEntity>()

            for (i in 0 until productsArray.length()) {
                val obj = productsArray.getJSONObject(i)
                val item = ProductEntity(
                    id = 0L, // Auto-generate clean id
                    name = obj.getString("name"),
                    barcode = obj.optString("barcode", ""),
                    category = obj.optString("category", "قرطاسية"),
                    purchaseUnit = obj.optString("purchaseUnit", "كرتونة"),
                    conversionFactor = obj.optInt("conversionFactor", 12),
                    purchaseCostPerUnit = obj.optDouble("purchaseCostPerUnit", 0.0),
                    costPerPiece = obj.optDouble("costPerPiece", 0.0),
                    saleUnit = obj.optString("saleUnit", "قطعة"),
                    salePricePerPiece = obj.optDouble("salePricePerPiece", 0.0),
                    salePricePerDozen = obj.optDouble("salePricePerDozen", 0.0),
                    salePricePerCarton = obj.optDouble("salePricePerCarton", 0.0),
                    stockPieces = obj.optInt("stockPieces", 0),
                    minStockAlert = obj.optInt("minStockAlert", 5)
                )
                importedList.add(item)
            }
            if (importedList.isNotEmpty()) {
                productDao.insertAll(importedList)
            }
            Result.success(importedList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class PurchaseItemInput(
    val productId: Long,
    val productName: String,
    val unitPurchased: String,
    val quantityUnits: Int,
    val conversionFactor: Int,
    val unitCost: Double
) {
    val totalCost: Double
        get() = quantityUnits * unitCost
}
