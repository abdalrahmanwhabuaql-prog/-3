package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductEntity
import com.example.data.model.StockMovementEntity
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.BarcodeVisualDisplay
import com.example.ui.theme.AlAqsaDarkGreen
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun InventoryScreen(
    viewModel: PosViewModel
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val totalCost by viewModel.totalInventoryCost.collectAsState()
    val movements by viewModel.allStockMovements.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productForBarcodeDisplay by remember { mutableStateOf<ProductEntity?>(null) }
    var productForMovements by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDeleteConfirm by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = allProducts.filter { p ->
        val matchesSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true)
        val matchesLowStock = !filterLowStockOnly || p.isLowStock
        matchesSearch && matchesLowStock
    }

    ScaffoldInventory(
        onAddNewProduct = { isAddingNewProduct = true }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Inventory Valuation Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي قيمة المخزون بسعر التكلفة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f", totalCost ?: 0.0),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlAqsaGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "عدد الأصناف",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${allProducts.size} صنف مسجل",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search & Low-stock toggle
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن صنف بالاسم، الباركود، أو التصنيف...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AlAqsaGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (filterLowStockOnly) Color(0xFFC62828) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "عرض المنتجات منخفضة المخزون فقط",
                                fontSize = 13.sp,
                                color = if (filterLowStockOnly) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = filterLowStockOnly,
                            onCheckedChange = { filterLowStockOnly = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFC62828))
                        )
                    }
                }
            }

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد نتائج تطابق شروط البحث",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        InventoryProductCard(
                            product = product,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDeleteConfirm = product },
                            onViewBarcode = { productForBarcodeDisplay = product },
                            onViewMovements = { productForMovements = product }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (isAddingNewProduct || productToEdit != null) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            onDismiss = {
                isAddingNewProduct = false
                productToEdit = null
            },
            onSave = { productToSave ->
                viewModel.saveProduct(productToSave) {
                    isAddingNewProduct = false
                    productToEdit = null
                }
            }
        )
    }

    // Barcode Display Dialog
    productForBarcodeDisplay?.let { p ->
        Dialog(onDismissRequest = { productForBarcodeDisplay = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "باركود الصنف",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BarcodeVisualDisplay(
                        barcode = p.barcode,
                        productName = p.name
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { productForBarcodeDisplay = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }

    // Stock Movements History Dialog
    productForMovements?.let { prod ->
        val prodMovements = movements.filter { it.productId == prod.id }
        StockMovementsDialog(
            product = prod,
            movements = prodMovements,
            onDismiss = { productForMovements = null }
        )
    }

    // Confirm Delete Dialog
    productToDeleteConfirm?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDeleteConfirm = null },
            title = { Text("حذف صنف من المخزون") },
            text = { Text("هل أنت متأكد من رغبتك في حذف الصنف (${prod.name})؟ لن تتأثر فواتير المبيعات السابقة.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDeleteConfirm = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ScaffoldInventory(
    onAddNewProduct: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content(PaddingValues(0.dp))
        FloatingActionButton(
            onClick = onAddNewProduct,
            containerColor = AlAqsaGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة منتج جديد")
        }
    }
}

@Composable
fun InventoryProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewBarcode: () -> Unit,
    onViewMovements: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AlAqsaGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 11.sp,
                                color = AlAqsaGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (product.barcode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بار: ${product.barcode}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Low stock badge
                if (product.isLowStock) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "مخزون منخفض",
                                color = Color(0xFFC62828),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Units info grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stock
                Column {
                    Text("الكمية المتوفرة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = product.formattedStock(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isLowStock) Color(0xFFC62828) else AlAqsaGreen
                    )
                }

                // Cost
                Column {
                    Text("سعر التكلفة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${String.format(Locale.US, "%.2f", product.costPerPiece)} للقطعة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Sale Price
                Column(horizontalAlignment = Alignment.End) {
                    Text("سعر البيع:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${String.format(Locale.US, "%.2f", product.salePricePerPiece)} للقطعة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onViewBarcode, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = "عرض الباركود", tint = AlAqsaGold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onViewMovements, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.History, contentDescription = "حركة المخزون", tint = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = AlAqsaGreen)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "قرطاسية عامة") }

    var purchaseUnit by remember { mutableStateOf(initialProduct?.purchaseUnit ?: "دستة (12 قلم)") }
    var conversionFactorText by remember { mutableStateOf(initialProduct?.conversionFactor?.toString() ?: "12") }
    var purchaseCostText by remember { mutableStateOf(initialProduct?.purchaseCostPerUnit?.toString() ?: "24.0") }
    var salePricePieceText by remember { mutableStateOf(initialProduct?.salePricePerPiece?.toString() ?: "3.0") }
    var salePriceBulkText by remember { mutableStateOf(initialProduct?.salePricePerDozen?.toString() ?: "32.0") }
    var stockPiecesText by remember { mutableStateOf(initialProduct?.stockPieces?.toString() ?: "36") }
    var minStockAlertText by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "6") }

    var showCameraScanner by remember { mutableStateOf(false) }

    // Calculated cost preview
    val conv = conversionFactorText.toIntOrNull() ?: 1
    val pCost = purchaseCostText.toDoubleOrNull() ?: 0.0
    val calculatedCostPerPiece = if (conv > 0) pCost / conv else pCost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialProduct == null) "إضافة صنف جديد للمكتبة" else "تعديل الصنف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlAqsaGreen
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج أو القرطاسية *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Barcode Row with Auto-Generate and Camera Scan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("الباركود") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(AlAqsaGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "مسح باركود", tint = AlAqsaGreen)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            // Generate unique standard barcode
                            barcode = "628" + String.format(Locale.US, "%09d", Random.nextInt(100000000, 999999999))
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(AlAqsaGold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "توليد باركود تلقائي", tint = AlAqsaGold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (أقلام، دفاتر، أوراق، فن، إلخ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "وحدة الشراء وتحويل الكميات (كرتونة / دستة / قطعة):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AlAqsaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseUnit,
                        onValueChange = { purchaseUnit = it },
                        label = { Text("وحدة الشراء") },
                        placeholder = { Text("دستة / كرتونة") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = conversionFactorText,
                        onValueChange = { conversionFactorText = it },
                        label = { Text("كم قطعة داخلها؟") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseCostText,
                        onValueChange = { purchaseCostText = it },
                        label = { Text("سعر شراء الوحدة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = salePricePieceText,
                        onValueChange = { salePricePieceText = it },
                        label = { Text("سعر بيع القطعة *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calculated Piece Cost Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AlAqsaGreen.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "التكلفة المحسوبة للقطعة الواحدة: ${String.format(Locale.US, "%.2f", calculatedCostPerPiece)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockPiecesText,
                        onValueChange = { stockPiecesText = it },
                        label = { Text("المخزون بالقطع *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockAlertText,
                        onValueChange = { minStockAlertText = it },
                        label = { Text("حد التنبيه") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val conversion = conversionFactorText.toIntOrNull() ?: 1
                            val pCostVal = purchaseCostText.toDoubleOrNull() ?: 0.0
                            val sPieceVal = salePricePieceText.toDoubleOrNull() ?: 0.0
                            val sBulkVal = salePriceBulkText.toDoubleOrNull() ?: 0.0
                            val stockPiecesVal = stockPiecesText.toIntOrNull() ?: 0
                            val minStockVal = minStockAlertText.toIntOrNull() ?: 5

                            val updated = (initialProduct ?: ProductEntity(name = name)).copy(
                                name = name.trim(),
                                barcode = barcode.trim(),
                                category = category.trim(),
                                purchaseUnit = purchaseUnit.trim(),
                                conversionFactor = conversion,
                                purchaseCostPerUnit = pCostVal,
                                costPerPiece = calculatedCostPerPiece,
                                salePricePerPiece = sPieceVal,
                                salePricePerDozen = sBulkVal,
                                stockPieces = stockPiecesVal,
                                minStockAlert = minStockVal
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        enabled = name.isNotBlank() && (salePricePieceText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("حفظ الصنف", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCameraScanner) {
        BarcodeScannerDialog(
            onDismiss = { showCameraScanner = false },
            onBarcodeScanned = { scanned ->
                // will be filled into barcode field
                showCameraScanner = false
            }
        )
    }
}

@Composable
fun StockMovementsDialog(
    product: ProductEntity,
    movements: List<StockMovementEntity>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "سجل حركة المخزون",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlAqsaGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                if (movements.isEmpty()) {
                    Text(
                        text = "لا توجد حركات مسجلة لهذا الصنف بعد",
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movements) { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = m.reasonOrNotes.ifBlank { m.movementType },
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = dateFormat.format(Date(m.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isPlus = m.quantityChangePieces > 0
                                    Text(
                                        text = "${if (isPlus) "+" else ""}${m.quantityChangePieces} قطعة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isPlus) AlAqsaGreen else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "الرصيد: ${m.resultingStockPieces}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق")
                }
            }
        }
    }
}
