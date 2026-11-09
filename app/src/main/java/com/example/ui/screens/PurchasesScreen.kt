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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
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
import com.example.data.model.PurchaseInvoiceEntity
import com.example.data.repository.PurchaseItemInput
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesScreen(
    viewModel: PosViewModel
) {
    val purchases by viewModel.allPurchases.collectAsState()
    val totalSupplierDebts by viewModel.totalSupplierDebts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var showNewInvoiceDialog by remember { mutableStateOf(false) }
    var invoiceToPayDebt by remember { mutableStateOf<PurchaseInvoiceEntity?>(null) }
    var payDebtAmountText by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Debt Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي ديون الموردين (ذمم متبقية)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", totalSupplierDebts ?: 0.0),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((totalSupplierDebts ?: 0.0) > 0) Color(0xFFC62828) else AlAqsaGreen
                        )
                    }

                    Button(
                        onClick = { showNewInvoiceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فاتورة شراء جديدة", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Purchase Invoices List
            if (purchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد فواتير مشتريات مسجلة بعد",
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
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases) { invoice ->
                        PurchaseInvoiceCard(
                            invoice = invoice,
                            dateFormat = dateFormat,
                            onPayDebtClick = {
                                invoiceToPayDebt = invoice
                                payDebtAmountText = invoice.remainingAmount.toString()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showNewInvoiceDialog = true },
            containerColor = AlAqsaGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "فاتورة شراء جديدة")
        }
    }

    // New Supplier Invoice Dialog
    if (showNewInvoiceDialog) {
        NewPurchaseInvoiceDialog(
            products = allProducts,
            onDismiss = { showNewInvoiceDialog = false },
            onSave = { supplierName, phone, paidAmount, notes, items ->
                viewModel.recordSupplierPurchase(
                    supplierName = supplierName,
                    supplierPhone = phone,
                    paidAmount = paidAmount,
                    notes = notes,
                    items = items,
                    onSuccess = { showNewInvoiceDialog = false }
                )
            }
        )
    }

    // Pay Debt Dialog
    invoiceToPayDebt?.let { inv ->
        AlertDialog(
            onDismissRequest = { invoiceToPayDebt = null },
            title = { Text("سداد دفعة للمورد: ${inv.supplierName}") },
            text = {
                Column {
                    Text("المبلغ المتبقي على الفاتورة (${inv.invoiceNumber}): ${String.format(Locale.US, "%.2f", inv.remainingAmount)}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = payDebtAmountText,
                        onValueChange = { payDebtAmountText = it },
                        label = { Text("المبلغ المدفوع الآن") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payDebtAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.paySupplierDebt(inv.id, inv.supplierName, amount)
                            invoiceToPayDebt = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen)
                ) {
                    Text("تأكيد السداد وحفظ بالصندوق")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToPayDebt = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun PurchaseInvoiceCard(
    invoice: PurchaseInvoiceEntity,
    dateFormat: SimpleDateFormat,
    onPayDebtClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المورد: ${invoice.supplierName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${invoice.invoiceNumber} • ${dateFormat.format(Date(invoice.timestamp))}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (invoice.remainingAmount <= 0) AlAqsaGreen.copy(alpha = 0.12f) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (invoice.remainingAmount <= 0) "خالصة الدفع" else "متبقي: ${String.format(Locale.US, "%.2f", invoice.remainingAmount)}",
                        color = if (invoice.remainingAmount <= 0) AlAqsaGreen else Color(0xFFC62828),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (invoice.supplierPhone.isNotBlank() || invoice.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (invoice.supplierPhone.isNotBlank()) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = invoice.supplierPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    if (invoice.notes.isNotBlank()) {
                        Text(text = "ملاحظات: ${invoice.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي الفاتورة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format(Locale.US, "%.2f", invoice.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AlAqsaGreen
                    )
                }

                Column {
                    Text("المدفوع:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format(Locale.US, "%.2f", invoice.paidAmount),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (invoice.remainingAmount > 0) {
                    Button(
                        onClick = onPayDebtClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سداد دفعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewPurchaseInvoiceDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, List<PurchaseItemInput>) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val itemsList = remember { androidx.compose.runtime.mutableStateListOf<PurchaseItemInput>() }

    // Dialog state for adding a line item
    var showAddItemDialog by remember { mutableStateOf(false) }

    val totalInvoiceCost = itemsList.sumOf { it.totalCost }

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
                    text = "تسجيل فاتورة شراء من مورد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlAqsaGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("اسم المورد أو الشركة *") },
                    placeholder = { Text("مثال: شركة القدس للأدوات المكتبية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supplierPhone,
                    onValueChange = { supplierPhone = it },
                    label = { Text("رقم هاتف المورد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Line items header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأصناف المشتراة (${itemsList.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { showAddItemDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة صنف")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (itemsList.isEmpty()) {
                    Text(
                        text = "لم يتم إضافة أصناف للفاتورة بعد (اضغط إضافة صنف)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    itemsList.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "${item.quantityUnits} ${item.unitPurchased} × ${String.format(Locale.US, "%.2f", item.unitCost)} = ${String.format(Locale.US, "%.2f", item.totalCost)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "+${item.quantityUnits * item.conversionFactor} قطعة ستضاف للمخزون",
                                        fontSize = 11.sp,
                                        color = AlAqsaGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { itemsList.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Payment Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي الفاتورة:", fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.US, "%.2f", totalInvoiceCost),
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = paidAmountText,
                    onValueChange = { paidAmountText = it },
                    label = { Text("المبلغ المدفوع نقداً للمورد الآن") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val paid = paidAmountText.toDoubleOrNull() ?: 0.0
                val remaining = (totalInvoiceCost - paid).coerceAtLeast(0.0)

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المبلغ المتبقي (دين للمورد): ${String.format(Locale.US, "%.2f", remaining)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (remaining > 0) Color(0xFFC62828) else AlAqsaGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الفاتورة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                            if (supplierName.isNotBlank() && itemsList.isNotEmpty()) {
                                onSave(
                                    supplierName.trim(),
                                    supplierPhone.trim(),
                                    paid,
                                    notes.trim(),
                                    itemsList.toList()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        enabled = supplierName.isNotBlank() && itemsList.isNotEmpty()
                    ) {
                        Text("حفظ الفاتورة وتحديث المخزون", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddPurchaseLineItemDialog(
            products = products,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItem ->
                itemsList.add(newItem)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun AddPurchaseLineItemDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onItemAdded: (PurchaseItemInput) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var unitPurchased by remember { mutableStateOf(selectedProduct?.purchaseUnit ?: "دستة (12 قلم)") }
    var conversionFactorText by remember { mutableStateOf(selectedProduct?.conversionFactor?.toString() ?: "12") }
    var quantityUnitsText by remember { mutableStateOf("1") }
    var unitCostText by remember { mutableStateOf(selectedProduct?.purchaseCostPerUnit?.toString() ?: "20.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "إضافة صنف مشتريات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("اختر المنتج من القائمة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(6.dp))

                // Product selection dropdown / list
                LazyColumn(modifier = Modifier.height(130.dp)) {
                    items(products) { prod ->
                        val isSelected = prod.id == selectedProduct?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedProduct = prod
                                    unitPurchased = prod.purchaseUnit
                                    conversionFactorText = prod.conversionFactor.toString()
                                    unitCostText = prod.purchaseCostPerUnit.toString()
                                }
                                .background(
                                    if (isSelected) AlAqsaGreen.copy(alpha = 0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prod.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AlAqsaGreen else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitPurchased,
                        onValueChange = { unitPurchased = it },
                        label = { Text("الوحدة") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = conversionFactorText,
                        onValueChange = { conversionFactorText = it },
                        label = { Text("معامل التحويل") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityUnitsText,
                        onValueChange = { quantityUnitsText = it },
                        label = { Text("الكمية المشتراة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it },
                        label = { Text("تكلفة الوحدة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                val qty = quantityUnitsText.toIntOrNull() ?: 1
                val factor = conversionFactorText.toIntOrNull() ?: 1
                val cost = unitCostText.toDoubleOrNull() ?: 0.0

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AlAqsaGreen.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الإجمالي: ${String.format(Locale.US, "%.2f", qty * cost)} (ستتم إضافة ${qty * factor} قطعة للمخزون)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            val prod = selectedProduct
                            if (prod != null && qty > 0 && cost > 0) {
                                onItemAdded(
                                    PurchaseItemInput(
                                        productId = prod.id,
                                        productName = prod.name,
                                        unitPurchased = unitPurchased.trim(),
                                        quantityUnits = qty,
                                        conversionFactor = factor,
                                        unitCost = cost
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        enabled = selectedProduct != null
                    ) {
                        Text("إضافة للفاتورة")
                    }
                }
            }
        }
    }
}
