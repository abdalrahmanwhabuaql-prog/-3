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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CashTransactionEntity
import com.example.ui.theme.AlAqsaDarkGreen
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashDrawerScreen(
    viewModel: PosViewModel
) {
    val cashBalance by viewModel.cashDrawerBalance.collectAsState()
    val transactions by viewModel.allCashTransactions.collectAsState()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showReconciliationDialog by remember { mutableStateOf(false) }

    val totalIn = transactions.filter { it.isCashIn }.sumOf { it.amount }
    val totalOut = transactions.filter { !it.isCashIn }.sumOf { it.amount }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Cash Register Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AlAqsaDarkGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "رصيد الصندوق النقدي الحالي",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AlAqsaGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "درج كاشير المكتبة",
                                color = AlAqsaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f", cashBalance),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي المقبوضات (داخل):", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                            Text(
                                text = String.format(Locale.US, "%.2f", totalIn),
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي المدفوعات (خارج):", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                            Text(
                                text = String.format(Locale.US, "%.2f", totalOut),
                                color = Color(0xFFEF9A9A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CashActionTile(
                        icon = Icons.Default.MoneyOff,
                        title = "مصروف يومي",
                        subtitle = "فواتير، إيجار، ضيافة",
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        onClick = { showExpenseDialog = true }
                    )
                    CashActionTile(
                        icon = Icons.Default.Person,
                        title = "سحب شخصي",
                        subtitle = "سحوبات عبدالرحمن",
                        color = Color(0xFFD84315),
                        modifier = Modifier.weight(1f),
                        onClick = { showDrawingDialog = true }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CashActionTile(
                        icon = Icons.Default.Print,
                        title = "إيراد إضافي",
                        subtitle = "طباعة، تصوير، خدمات",
                        color = AlAqsaGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { showIncomeDialog = true }
                    )
                    CashActionTile(
                        icon = Icons.Default.Tune,
                        title = "مطابقة الصندوق",
                        subtitle = "الجرد ومقارنة الفعلي",
                        color = AlAqsaGold,
                        modifier = Modifier.weight(1f),
                        onClick = { showReconciliationDialog = true }
                    )
                }
            }
        }

        // Transactions History Header
        item {
            Text(
                text = "سجل حركة الصندوق (${transactions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "لا توجد حركات مسجلة بالصندوق بعد",
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(transactions) { trans ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (trans.isCashIn) AlAqsaGreen.copy(alpha = 0.12f) else Color(0xFFFFEBEE),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (trans.isCashIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (trans.isCashIn) AlAqsaGreen else Color(0xFFC62828),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = trans.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${dateFormat.format(Date(trans.timestamp))} • ${formatTransactionType(trans.type)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                if (trans.notes.isNotBlank()) {
                                    Text(
                                        text = trans.notes,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${if (trans.isCashIn) "+" else "-"}${String.format(Locale.US, "%.2f", trans.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (trans.isCashIn) AlAqsaGreen else Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Record Expense Dialog
    if (showExpenseDialog) {
        RecordCashMovementDialog(
            title = "تسجيل مصروف يومي للمكتبة",
            type = "EXPENSE",
            defaultCategory = "مصروفات عامة",
            categories = listOf("مصروفات عامة", "فاتورة كهرباء", "فاتورة مياه/إنترنت", "إيجار المحل", "نقل وتوصيل", "ضيافة ونظافة", "قرطاسية وأدوات تشغيل"),
            onDismiss = { showExpenseDialog = false },
            onSave = { desc, amount, cat, notes ->
                viewModel.addCashTransaction("EXPENSE", desc, amount, cat, notes)
                showExpenseDialog = false
            }
        )
    }

    // Record Personal Drawing Dialog (سحوبات شخصية)
    if (showDrawingDialog) {
        RecordCashMovementDialog(
            title = "تسجيل سحب شخصي (عبدالرحمن)",
            type = "DRAWING",
            defaultCategory = "سحوبات شخصية",
            categories = listOf("سحوبات شخصية", "مصاريف خاصة", "سلفة شخصية"),
            onDismiss = { showDrawingDialog = false },
            onSave = { desc, amount, cat, notes ->
                viewModel.addCashTransaction("DRAWING", desc, amount, cat, notes)
                showDrawingDialog = false
            }
        )
    }

    // Record Other Income Dialog (إيرادات أخرى)
    if (showIncomeDialog) {
        RecordCashMovementDialog(
            title = "تسجيل إيراد إضافي",
            type = "OTHER_INCOME",
            defaultCategory = "خدمات طباعة وتصوير",
            categories = listOf("خدمات طباعة وتصوير", "تجليد كتب وأبحاث", "مبيعات خدمات", "إيراد آخر"),
            onDismiss = { showIncomeDialog = false },
            onSave = { desc, amount, cat, notes ->
                viewModel.addCashTransaction("OTHER_INCOME", desc, amount, cat, notes)
                showIncomeDialog = false
            }
        )
    }

    // Cash Reconciliation Dialog
    if (showReconciliationDialog) {
        CashReconciliationDialog(
            systemBalance = cashBalance,
            onDismiss = { showReconciliationDialog = false },
            onSaveAdjustment = { diff ->
                if (diff != 0.0) {
                    val isIncome = diff > 0
                    val type = if (isIncome) "OTHER_INCOME" else "EXPENSE"
                    val desc = if (isIncome) "تسوية جرد الصندوق (فائض نقدي)" else "تسوية جرد الصندوق (عجز نقدي)"
                    viewModel.addCashTransaction(
                        type = type,
                        title = desc,
                        amount = kotlin.math.abs(diff),
                        category = "تسوية صندوق",
                        notes = "تمت المطابقة مع النقد الفعلي بالدرج"
                    )
                }
                showReconciliationDialog = false
            }
        )
    }
}

private fun formatTransactionType(type: String): String {
    return when (type) {
        "SALE_CASH" -> "مبيعات نقدية"
        "SALE_CANCEL" -> "إلغاء بيع"
        "EXPENSE" -> "مصروف يومي"
        "DRAWING" -> "سحب شخصي"
        "OTHER_INCOME" -> "إيراد إضافي"
        "PURCHASE_PAYMENT" -> "دفعة مورد"
        else -> type
    }
}

@Composable
fun CashActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun RecordCashMovementDialog(
    title: String,
    type: String,
    defaultCategory: String,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(defaultCategory) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AlAqsaGreen)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("بيان الحركة *") },
                    placeholder = { Text("مثال: فاتورة كهرباء شهر مايو") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ *") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (desc.isNotBlank() && amt > 0) {
                                onSave(desc.trim(), amt, selectedCat, notes.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        enabled = desc.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("حفظ الحركة")
                    }
                }
            }
        }
    }
}

@Composable
fun CashReconciliationDialog(
    systemBalance: Double,
    onDismiss: () -> Unit,
    onSaveAdjustment: (Double) -> Unit
) {
    var countedCashText by remember { mutableStateOf("") }

    val countedCash = countedCashText.toDoubleOrNull() ?: 0.0
    val difference = if (countedCashText.isNotBlank()) countedCash - systemBalance else 0.0

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
                    text = "مطابقة وجرد الصندوق النقدي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlAqsaGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الرصيد المسجل في النظام: ${String.format(Locale.US, "%.2f", systemBalance)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = countedCashText,
                    onValueChange = { countedCashText = it },
                    label = { Text("النقد الفعلي المعدود بالدرج") },
                    placeholder = { Text("مثال: 450.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (countedCashText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            kotlin.math.abs(difference) < 0.01 -> AlAqsaGreen.copy(alpha = 0.12f)
                            difference > 0 -> Color(0xFFE8F5E9)
                            else -> Color(0xFFFFEBEE)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val statusText = when {
                                kotlin.math.abs(difference) < 0.01 -> "الرصيد مطابق تماماً! لا يوجد فرق."
                                difference > 0 -> "يوجد فائض نقدي بالدرج بمقدار: +${String.format(Locale.US, "%.2f", difference)}"
                                else -> "يوجد عجز نقدي بالدرج بمقدار: ${String.format(Locale.US, "%.2f", difference)}"
                            }
                            Text(
                                text = statusText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (difference < 0) Color(0xFFC62828) else AlAqsaGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إغلاق") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSaveAdjustment(difference) },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        enabled = countedCashText.isNotBlank()
                    ) {
                        Text("تسجيل التسوية")
                    }
                }
            }
        }
    }
}
