package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.ProductEntity
import com.example.data.repository.CartItem
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CameraXBarcodeScannerPanel
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCashierScreen(
    viewModel: PosViewModel
) {
    val products by viewModel.posFilteredProducts.collectAsState()
    val cart by viewModel.cartItems.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCategory by viewModel.posSelectedCategory.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val customerName by viewModel.customerName.collectAsState()

    var isCameraScannerOpen by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    var selectedProductForUnitDialog by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf(
        "الكل",
        "أقلام ومحايات",
        "دفاتر ومذكرات",
        "أوراق وطباعة",
        "أدوات فنية ورسم",
        "أجهزة وإلكترونيات",
        "كتب ومصاحف",
        "ملفات وتنظيم",
        "لوازم مكتبية ولواصق"
    )

    val cartTotal = cart.sumOf { it.lineTotal }
    val cartItemsCount = cart.sumOf { it.quantityUnits }.toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Barcode Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.posSearchQuery.value = it },
                            placeholder = { Text("بحث بالاسم أو الباركود...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AlAqsaGreen)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.posSearchQuery.value = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Toggle Live Camera Scanner directly in the screen
                        IconButton(
                            onClick = { isCameraScannerOpen = !isCameraScannerOpen },
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (isCameraScannerOpen) AlAqsaGold else AlAqsaGreen,
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "تشغيل كاميرا مسح الباركود",
                                tint = if (isCameraScannerOpen) Color.Black else Color.White
                            )
                        }
                    }

                    // Integrated Live Camera Scanner Panel (CameraX + ML Kit)
                    AnimatedVisibility(visible = isCameraScannerOpen) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            CameraXBarcodeScannerPanel(
                                onBarcodeScanned = { rawCode ->
                                    viewModel.handleScannedBarcode(rawCode)
                                },
                                onClose = { isCameraScannerOpen = false }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            modifier = Modifier.clickable { isCameraScannerOpen = !isCameraScannerOpen },
                            color = if (isCameraScannerOpen) AlAqsaGold else AlAqsaGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (isCameraScannerOpen) Color.Black else AlAqsaGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCameraScannerOpen) "إخفاء كاميرا المسح" else "كاميرا المسح المباشر (CameraX)",
                                    color = if (isCameraScannerOpen) Color.Black else AlAqsaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Text(
                            text = if (isCameraScannerOpen) "المسح المباشر نشط" else "${products.size} صنف متوفر",
                            fontSize = 11.sp,
                            color = if (isCameraScannerOpen) AlAqsaGreen else MaterialTheme.colorScheme.outline,
                            fontWeight = if (isCameraScannerOpen) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Categories Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.posSelectedCategory.value = cat },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AlAqsaGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Products Catalog
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد منتجات مطابقة للبحث",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductPosItemCard(
                            product = product,
                            onQuickAddPiece = { viewModel.addToCart(product, "قطعة") },
                            onSelectUnit = { selectedProductForUnitDialog = product }
                        )
                    }
                    // Bottom spacing so FAB / BottomBar doesn't occlude last items
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Sticky Bottom Cart Bar if items exist
        AnimatedVisibility(
            visible = cart.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartSheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AlAqsaGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AlAqsaGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartItemsCount",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "سلة المشتريات",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${cart.size} أصناف مختلفة",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "%.2f", cartTotal),
                            color = AlAqsaGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إتمام البيع", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Unit Selection Dialog (e.g. choose Piece vs Dozen vs Carton)
    selectedProductForUnitDialog?.let { prod ->
        ProductUnitSelectorDialog(
            product = prod,
            onDismiss = { selectedProductForUnitDialog = null },
            onUnitSelected = { unitType ->
                viewModel.addToCart(prod, unitType)
                selectedProductForUnitDialog = null
            }
        )
    }

    // Modal Bottom Sheet for Cart Checkout
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CartCheckoutContent(
                cartItems = cart,
                totalAmount = cartTotal,
                paymentMethod = paymentMethod,
                customerName = customerName,
                onUpdateQty = { id, unit, qty -> viewModel.updateCartItemQuantity(id, unit, qty) },
                onRemoveItem = { id, unit -> viewModel.removeFromCart(id, unit) },
                onClearCart = { viewModel.clearCart() },
                onPaymentMethodChange = { viewModel.paymentMethod.value = it },
                onCustomerNameChange = { viewModel.customerName.value = it },
                onCompleteSale = {
                    viewModel.completeSale { _, _ ->
                        showCartSheet = false
                    }
                }
            )
        }
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false },
            onBarcodeScanned = { code ->
                viewModel.handleScannedBarcode(code)
            }
        )
    }
}

@Composable
fun ProductPosItemCard(
    product: ProductEntity,
    onQuickAddPiece: () -> Unit,
    onSelectUnit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "المتوفر: ${product.stockPieces} قطعة",
                        fontSize = 12.sp,
                        color = if (product.isLowStock) Color(0xFFC62828) else AlAqsaGreen,
                        fontWeight = if (product.isLowStock) FontWeight.Bold else FontWeight.Normal
                    )
                    if (product.conversionFactor > 1) {
                        Text(
                            text = " • (${product.purchaseUnit})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.US, "%.2f", product.salePricePerPiece)} للقطعة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                    if (product.costPerPiece > 0) {
                        Text(
                            text = " (تكلفة: ${String.format(Locale.US, "%.2f", product.costPerPiece)})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // If product has carton/dozen conversion, offer unit options button
                if (product.conversionFactor > 1) {
                    OutlinedButton(
                        onClick = onSelectUnit,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text("وحدات", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onQuickAddPiece,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductUnitSelectorDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onUnitSelected: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "اختر وحدة البيع لـ:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Piece (القطعة)
                UnitOptionButton(
                    unitName = "بالقطعة المفردة",
                    unitSubtext = "1 قطعة = ${String.format(Locale.US, "%.2f", product.salePricePerPiece)}",
                    onClick = { onUnitSelected("قطعة") }
                )

                // Option 2: Dozen if applicable
                if (product.conversionFactor >= 12 || product.salePricePerDozen > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val dozenPrice = if (product.salePricePerDozen > 0) product.salePricePerDozen else product.salePricePerPiece * 12
                    UnitOptionButton(
                        unitName = "بالدستة (12 قطعة)",
                        unitSubtext = "12 قطعة = ${String.format(Locale.US, "%.2f", dozenPrice)}",
                        onClick = { onUnitSelected("دستة") }
                    )
                }

                // Option 3: Purchase Unit / Carton
                if (product.conversionFactor > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val cartonPrice = if (product.salePricePerCarton > 0) product.salePricePerCarton else product.salePricePerPiece * product.conversionFactor
                    UnitOptionButton(
                        unitName = "بال${product.purchaseUnit} (${product.conversionFactor} قطعة)",
                        unitSubtext = "${product.conversionFactor} قطعة = ${String.format(Locale.US, "%.2f", cartonPrice)}",
                        onClick = { onUnitSelected("كرتونة") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إلغاء")
                }
            }
        }
    }
}

@Composable
private fun UnitOptionButton(unitName: String, unitSubtext: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = unitName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = unitSubtext, fontSize = 12.sp, color = AlAqsaGreen)
            }
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AlAqsaGreen)
        }
    }
}

@Composable
fun CartCheckoutContent(
    cartItems: List<CartItem>,
    totalAmount: Double,
    paymentMethod: String,
    customerName: String,
    onUpdateQty: (Long, String, Double) -> Unit,
    onRemoveItem: (Long, String) -> Unit,
    onClearCart: () -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCompleteSale: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = AlAqsaGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "سلة المشتريات (${cartItems.size} أصناف)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            TextButton(onClick = onClearCart) {
                Text("تفريغ السلة", color = Color(0xFFC62828))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        // Items list
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cartItems) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "وحدة: ${item.unitType} • سعر: ${String.format(Locale.US, "%.2f", item.unitPrice)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "الإجمالي: ${String.format(Locale.US, "%.2f", item.lineTotal)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlAqsaGreen
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onUpdateQty(item.product.id, item.unitType, item.quantityUnits - 1.0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "إنقاص")
                            }
                            Text(
                                text = "${item.quantityUnits.toInt()}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { onUpdateQty(item.product.id, item.unitType, item.quantityUnits + 1.0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة")
                            }
                            IconButton(
                                onClick = { onRemoveItem(item.product.id, item.unitType) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        // Payment Method Radio
        Text(text = "طريقة الدفع:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onPaymentMethodChange("CASH") }) {
                RadioButton(
                    selected = paymentMethod == "CASH",
                    onClick = { onPaymentMethodChange("CASH") },
                    colors = RadioButtonDefaults.colors(selectedColor = AlAqsaGreen)
                )
                Text("نقدي (كاش)")
            }
            Spacer(modifier = Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onPaymentMethodChange("CREDIT") }) {
                RadioButton(
                    selected = paymentMethod == "CREDIT",
                    onClick = { onPaymentMethodChange("CREDIT") },
                    colors = RadioButtonDefaults.colors(selectedColor = AlAqsaGreen)
                )
                Text("آجل (ذمم زبائن)")
            }
        }

        if (paymentMethod == "CREDIT") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customerName,
                onValueChange = onCustomerNameChange,
                label = { Text("اسم الزبون (إجباري للآجل)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Checkout Button & Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "المبلغ الإجمالي:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = String.format(Locale.US, "%.2f", totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlAqsaGreen
                )
            }
            Button(
                onClick = onCompleteSale,
                colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل البيع وحفظ بالصندوق", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
