package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CashTransactionEntity
import com.example.data.model.ProductEntity
import com.example.data.model.PurchaseInvoiceEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.SupplierSummary
import com.example.data.model.TopProductSummary
import com.example.data.repository.CartItem
import com.example.data.repository.PosRepository
import com.example.data.repository.PurchaseItemInput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowSaleSuccess(val invoiceNumber: String, val total: Double) : UiEvent()
}

enum class ReportPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = PosRepository(db)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Navigation tab: 0: Dashboard, 1: POS, 2: Inventory, 3: Purchases, 4: Cash Drawer, 5: Reports
    val selectedTab = MutableStateFlow(0)

    // Products & Inventory
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalInventoryCost: StateFlow<Double?> = repository.totalInventoryCost
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSupplierDebts: StateFlow<Double?> = repository.totalSupplierDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Sales & Movements
    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseInvoiceEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierSummaries: StateFlow<List<SupplierSummary>> = repository.supplierSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCashTransactions: StateFlow<List<CashTransactionEntity>> = repository.allCashTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockMovements: StateFlow<List<StockMovementEntity>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topProductsAllTime: StateFlow<List<TopProductSummary>> = repository.getTopProducts(0)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Screen State
    val posSearchQuery = MutableStateFlow("")
    val posSelectedCategory = MutableStateFlow("الكل")
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val paymentMethod = MutableStateFlow("CASH") // CASH or CREDIT
    val customerName = MutableStateFlow("")

    // Filtered Products for POS
    val posFilteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        posSearchQuery,
        posSelectedCategory
    ) { products, query, category ->
        products.filter { p ->
            val matchesCategory = category == "الكل" || p.category == category
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash Register Calculated Balance
    val cashDrawerBalance: StateFlow<Double> = allCashTransactions.combine(allCashTransactions) { _, _ ->
        val transactions = repository.allCashTransactions
        // We calculate directly from transactions
        0.0
    }.combine(allCashTransactions) { _, transList ->
        var balance = 0.0
        transList.forEach {
            if (it.isCashIn) balance += it.amount else balance -= it.amount
        }
        balance
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Reports filter period
    val selectedReportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val reportStagnantProducts = MutableStateFlow<List<ProductEntity>>(emptyList())

    init {
        viewModelScope.launch {
            repository.initSampleDataIfNeeded()
            loadStagnantProducts()
        }
    }

    private fun vibrateDevice() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(50)
                }
            }
        } catch (_: Exception) {}
    }

    // --- POS & Cart Actions ---
    fun addToCart(product: ProductEntity, unitType: String = "قطعة") {
        vibrateDevice()
        val currentList = cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id && it.unitType == unitType }

        val piecesPerUnit = when (unitType) {
            "كرتونة" -> product.conversionFactor
            "دستة" -> if (product.conversionFactor >= 12) 12 else product.conversionFactor
            else -> 1
        }

        val unitPrice = when (unitType) {
            "كرتونة" -> if (product.salePricePerCarton > 0) product.salePricePerCarton else product.salePricePerPiece * piecesPerUnit
            "دستة" -> if (product.salePricePerDozen > 0) product.salePricePerDozen else product.salePricePerPiece * piecesPerUnit
            else -> product.salePricePerPiece
        }

        val unitCost = product.costPerPiece * piecesPerUnit

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val updated = existing.copy(quantityUnits = existing.quantityUnits + 1.0)
            currentList[existingIndex] = updated
        } else {
            currentList.add(
                CartItem(
                    product = product,
                    unitType = unitType,
                    piecesPerUnit = piecesPerUnit,
                    quantityUnits = 1.0,
                    unitPrice = unitPrice,
                    unitCost = unitCost
                )
            )
        }
        cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, unitType: String, newQty: Double) {
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId && it.unitType == unitType }
        if (index >= 0) {
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantityUnits = newQty)
            }
            cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long, unitType: String) {
        val currentList = cartItems.value.filterNot { it.product.id == productId && it.unitType == unitType }
        cartItems.value = currentList
    }

    fun clearCart() {
        cartItems.value = emptyList()
        customerName.value = ""
        paymentMethod.value = "CASH"
    }

    fun handleScannedBarcode(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val product = repository.getProductByBarcode(trimmed)
            if (product != null) {
                addToCart(product, "قطعة")
                _eventFlow.emit(UiEvent.ShowToast("تمت إضافة: ${product.name}"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("الباركود غير مسجل: $trimmed"))
            }
        }
    }

    fun completeSale(onSuccess: (String, Double) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) return

        val method = paymentMethod.value
        val customer = customerName.value.trim()
        if (method == "CREDIT" && customer.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowToast("يرجى كتابة اسم الزبون للبيع الآجل"))
            }
            return
        }

        viewModelScope.launch {
            val total = items.sumOf { it.lineTotal }
            val saleId = repository.processSale(items, method, customer)
            val saleWithItems = repository.getSaleWithItems(saleId)
            val invNum = saleWithItems.first?.invoiceNumber ?: "INV-$saleId"

            clearCart()
            vibrateDevice()
            _eventFlow.emit(UiEvent.ShowSaleSuccess(invNum, total))
            onSuccess(invNum, total)
        }
    }

    fun loadSaleDetails(saleId: Long, onLoaded: (List<SaleItemEntity>) -> Unit) {
        viewModelScope.launch {
            val res = repository.getSaleWithItems(saleId)
            onLoaded(res.second)
        }
    }

    fun cancelSale(saleId: Long, reason: String) {
        if (reason.isBlank()) return
        viewModelScope.launch {
            repository.cancelSale(saleId, reason)
            _eventFlow.emit(UiEvent.ShowToast("تم إلغاء عملية البيع واسترجاع المخزون بنجاح"))
        }
    }

    // --- Product CRUD ---
    fun saveProduct(product: ProductEntity, onSaved: () -> Unit) {
        if (product.name.isBlank()) return
        viewModelScope.launch {
            repository.saveProduct(product)
            _eventFlow.emit(UiEvent.ShowToast("تم حفظ المنتج: ${product.name}"))
            onSaved()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _eventFlow.emit(UiEvent.ShowToast("تم حذف المنتج: ${product.name}"))
        }
    }

    // --- Supplier Purchases ---
    fun recordSupplierPurchase(
        supplierName: String,
        supplierPhone: String,
        paidAmount: Double,
        notes: String,
        items: List<PurchaseItemInput>,
        onSuccess: () -> Unit
    ) {
        if (supplierName.isBlank() || items.isEmpty()) return
        viewModelScope.launch {
            repository.recordPurchaseInvoice(supplierName, supplierPhone, paidAmount, notes, items)
            _eventFlow.emit(UiEvent.ShowToast("تم تسجيل فاتورة الشراء وتحديث المخزون بنجاح"))
            onSuccess()
        }
    }

    fun paySupplierDebt(invoiceId: Long, supplierName: String, payAmount: Double) {
        if (payAmount <= 0) return
        viewModelScope.launch {
            repository.paySupplierDebt(invoiceId, supplierName, payAmount)
            _eventFlow.emit(UiEvent.ShowToast("تم سداد مبلغ $payAmount للمورد $supplierName"))
        }
    }

    // --- Cash Drawer & Expenses ---
    fun addCashTransaction(type: String, title: String, amount: Double, category: String, notes: String) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.addCashTransaction(type, title, amount, category, notes)
            val typeLabel = when (type) {
                "EXPENSE" -> "مصروف"
                "DRAWING" -> "سحب شخصي"
                else -> "إيراد"
            }
            _eventFlow.emit(UiEvent.ShowToast("تم تسجيل $typeLabel بمبلغ $amount بنجاح"))
        }
    }

    // --- Reports Helper ---
    fun getPeriodStartTimestamp(period: ReportPeriod): Long {
        val cal = Calendar.getInstance()
        when (period) {
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.ALL_TIME -> return 0L
        }
        return cal.timeInMillis
    }

    fun loadStagnantProducts() {
        viewModelScope.launch {
            val monthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            reportStagnantProducts.value = repository.getStagnantProducts(monthAgo)
        }
    }

    // --- Backup & Restore ---
    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDatabaseToJson()
            onExported(json)
            _eventFlow.emit(UiEvent.ShowToast("تم تصدير النسخة الاحتياطية بنجاح"))
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val result = repository.importDatabaseFromJson(jsonString)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _eventFlow.emit(UiEvent.ShowToast("تم استيراد $count منتج بنجاح"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("فشل استيراد النسخة الاحتياطية"))
            }
        }
    }
}
