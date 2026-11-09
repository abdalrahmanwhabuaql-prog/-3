package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.TopProductSummary
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.SaleDetailDialog
import com.example.ui.theme.AlAqsaDarkGreen
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGoldBright
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.ReportPeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PosViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToCash: () -> Unit
) {
    val sales by viewModel.allSales.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val cashBalance by viewModel.cashDrawerBalance.collectAsState()
    val topProducts by viewModel.topProductsAllTime.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    var selectedSaleForDetails by remember { mutableStateOf<SaleEntity?>(null) }
    var selectedSaleItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }

    val todayStart = remember { viewModel.getPeriodStartTimestamp(ReportPeriod.TODAY) }
    val todaySales = sales.filter { it.timestamp >= todayStart && !it.isCancelled }
    val todaySalesTotal = todaySales.sumOf { it.totalAmount }
    val todaySalesCount = todaySales.size
    val todayProfit = todaySales.sumOf { it.grossProfit }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Maktabat Al-Aqsa
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AlAqsaDarkGreen, AlAqsaGreen, Color(0xFF0F6841))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مكتبة الأقصى – عبدالرحمن",
                                color = AlAqsaGoldBright,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "نظام الكاشير وإدارة المخزون والمبيعات",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = AlAqsaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "وضع العمل دون إنترنت • 100% محلي",
                                    color = AlAqsaGoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Official Logo emblem
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.al_aqsa_logo),
                                contentDescription = "شعار مكتبة الأقصى الرسمي",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.PointOfSale,
                    label = "بيع سريع",
                    containerColor = AlAqsaGreen,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPos
                )
                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "مسح باركود",
                    containerColor = AlAqsaGold,
                    contentColor = Color(0xFF332000),
                    modifier = Modifier.weight(1f),
                    onClick = { showScanner = true }
                )
                QuickActionButton(
                    icon = Icons.Default.AddBusiness,
                    label = "فاتورة شراء",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPurchases
                )
            }
        }

        // Key KPI Dashboard Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "مبيعات اليوم",
                        value = String.format(Locale.US, "%.2f", todaySalesTotal),
                        subtext = "$todaySalesCount عملية بيع",
                        icon = Icons.Default.AttachMoney,
                        accentColor = AlAqsaGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "أرباح اليوم",
                        value = String.format(Locale.US, "+%.2f", todayProfit),
                        subtext = "صافي هامش الربح",
                        icon = Icons.Default.TrendingUp,
                        accentColor = AlAqsaGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "رصيد الصندوق",
                        value = String.format(Locale.US, "%.2f", cashBalance),
                        subtext = "النقد المتوفر بالدرج",
                        icon = Icons.Default.Receipt,
                        accentColor = if (cashBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCash
                    )
                    KpiMetricCard(
                        title = "نواقص المخزون",
                        value = "${lowStock.size}",
                        subtext = if (lowStock.isEmpty()) "المخزون ممتاز" else "تنبيه لإعادة الطلب",
                        icon = Icons.Default.Warning,
                        accentColor = if (lowStock.isEmpty()) AlAqsaGreen else Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInventory
                    )
                }
            }
        }

        // Low stock warning banner if any
        if (lowStock.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه: ${lowStock.size} منتجات قاربت على النفاد!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "اضغط هنا لعرض النواقص وإجراء طلبية مورد جديدة",
                                color = Color(0xFFBF360C),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Top Selling Products Section
        if (topProducts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المنتجات الأكثر طلباً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عرض الكل",
                            color = AlAqsaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToInventory() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(topProducts.take(6)) { topItem ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.width(150.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = topItem.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${topItem.totalPiecesSold} قطعة مباعة",
                                        color = AlAqsaGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "إيراد: ${String.format(Locale.US, "%.1f", topItem.totalRevenue)}",
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Sales Stream
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر العمليات المسجلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sales.size} عملية مسجلة",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }
        }

        if (sales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد مبيعات مسجلة حتى الآن",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToPos,
                            colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen)
                        ) {
                            Text("فتح شاشة الكاشير للبيع")
                        }
                    }
                }
            }
        } else {
            items(sales.take(8)) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.loadSaleDetails(sale.id) { items ->
                                selectedSaleItems = items
                                selectedSaleForDetails = sale
                            }
                        },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (sale.isCancelled) Color(0xFFFFEBEE) else AlAqsaGreen.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (sale.isCancelled) Icons.Default.Warning else Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = if (sale.isCancelled) Color(0xFFC62828) else AlAqsaGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = sale.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (sale.isCancelled) "ملغاة (${sale.cancellationReason ?: ""})"
                                    else "${timeFormat.format(Date(sale.timestamp))} • ${if (sale.paymentMethod == "CASH") "نقدي" else "آجل: " + sale.customerName}",
                                    fontSize = 12.sp,
                                    color = if (sale.isCancelled) Color(0xFFC62828) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%.2f", sale.totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (sale.isCancelled) Color.Gray else AlAqsaGreen
                            )
                            if (!sale.isCancelled) {
                                Text(
                                    text = "ربح: ${String.format(Locale.US, "+%.2f", sale.grossProfit)}",
                                    fontSize = 11.sp,
                                    color = AlAqsaGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { showScanner = false },
            onBarcodeScanned = { code ->
                viewModel.handleScannedBarcode(code)
                onNavigateToPos()
            }
        )
    }

    selectedSaleForDetails?.let { sale ->
        SaleDetailDialog(
            sale = sale,
            items = selectedSaleItems,
            onDismiss = { selectedSaleForDetails = null },
            onCancelSale = { id, reason ->
                viewModel.cancelSale(id, reason)
                selectedSaleForDetails = null
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
