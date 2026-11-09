package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import com.example.data.model.TopProductSummary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.ReportPeriod
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: PosViewModel
) {
    val selectedPeriod by viewModel.selectedReportPeriod.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val allTransactions by viewModel.allCashTransactions.collectAsState()
    val topProducts by viewModel.topProductsAllTime.collectAsState()
    val stagnantProducts by viewModel.reportStagnantProducts.collectAsState()
    val totalInventoryCost by viewModel.totalInventoryCost.collectAsState()

    val periodStart = viewModel.getPeriodStartTimestamp(selectedPeriod)

    // Filter sales within period that are not cancelled
    val periodSales = allSales.filter { it.timestamp >= periodStart && !it.isCancelled }
    val totalRevenue = periodSales.sumOf { it.totalAmount }
    val totalCostOfGoodsSold = periodSales.sumOf { it.totalCost }
    val grossProfit = totalRevenue - totalCostOfGoodsSold

    // Filter expenses & personal drawings within period
    val periodExpenses = allTransactions
        .filter { it.timestamp >= periodStart && it.type == "EXPENSE" }
        .sumOf { it.amount }

    val periodDrawings = allTransactions
        .filter { it.timestamp >= periodStart && it.type == "DRAWING" }
        .sumOf { it.amount }

    val periodOtherIncome = allTransactions
        .filter { it.timestamp >= periodStart && it.type == "OTHER_INCOME" }
        .sumOf { it.amount }

    // Net Profit = Gross Profit + Other Income - Operating Expenses
    val netProfit = (grossProfit + periodOtherIncome) - periodExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Period Selector Tabs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                TabRow(
                    selectedTabIndex = selectedPeriod.ordinal,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedPeriod.ordinal]),
                            color = AlAqsaGreen
                        )
                    }
                ) {
                    Tab(
                        selected = selectedPeriod == ReportPeriod.TODAY,
                        onClick = { viewModel.selectedReportPeriod.value = ReportPeriod.TODAY },
                        text = { Text("اليوم", fontWeight = if (selectedPeriod == ReportPeriod.TODAY) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedPeriod == ReportPeriod.THIS_WEEK,
                        onClick = { viewModel.selectedReportPeriod.value = ReportPeriod.THIS_WEEK },
                        text = { Text("هذا الأسبوع", fontWeight = if (selectedPeriod == ReportPeriod.THIS_WEEK) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedPeriod == ReportPeriod.THIS_MONTH,
                        onClick = { viewModel.selectedReportPeriod.value = ReportPeriod.THIS_MONTH },
                        text = { Text("هذا الشهر", fontWeight = if (selectedPeriod == ReportPeriod.THIS_MONTH) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedPeriod == ReportPeriod.ALL_TIME,
                        onClick = { viewModel.selectedReportPeriod.value = ReportPeriod.ALL_TIME },
                        text = { Text("الكل", fontWeight = if (selectedPeriod == ReportPeriod.ALL_TIME) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Financial Highlights Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Net Profit Card (Big Hero)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) AlAqsaGreen else Color(0xFFC62828)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "صافي الربح الفعلي للفترة",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f", netProfit),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الربح الإجمالي - المصروفات التشغيلية",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = AlAqsaGold,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Breakdown rows
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportMetricCard(
                        title = "المبيعات الإجمالية",
                        value = String.format(Locale.US, "%.2f", totalRevenue),
                        subtext = "${periodSales.size} عملية بيع",
                        icon = Icons.Default.ShoppingCart,
                        accentColor = AlAqsaGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "تكلفة البضاعة المباعة (COGS)",
                        value = String.format(Locale.US, "%.2f", totalCostOfGoodsSold),
                        subtext = "سعر شراء ما تم بيعه",
                        icon = Icons.Default.Inventory,
                        accentColor = Color(0xFF5D4037),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportMetricCard(
                        title = "إجمالي الربح (Gross)",
                        value = String.format(Locale.US, "+%.2f", grossProfit),
                        subtext = "المبيعات - تكلفة البضاعة",
                        icon = Icons.Default.TrendingUp,
                        accentColor = AlAqsaGold,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "المصروفات اليومية",
                        value = String.format(Locale.US, "%.2f", periodExpenses),
                        subtext = "فواتير ومصاريف المحل",
                        icon = Icons.Default.MoneyOff,
                        accentColor = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportMetricCard(
                        title = "السحوبات الشخصية",
                        value = String.format(Locale.US, "%.2f", periodDrawings),
                        subtext = "سحوبات عبدالرحمن",
                        icon = Icons.Default.Person,
                        accentColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "قيمة المخزون الحالي",
                        value = String.format(Locale.US, "%.2f", totalInventoryCost ?: 0.0),
                        subtext = "رأس مال البضاعة بالمخزن",
                        icon = Icons.Default.AccountBalance,
                        accentColor = Color(0xFF00695C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Top Selling Products Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المنتجات الأكثر مبيعاً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = AlAqsaGreen)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (topProducts.isEmpty()) {
                        Text("لا توجد مبيعات مسجلة حتى الآن", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
                    } else {
                        topProducts.take(5).forEachIndexed { index, top ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(AlAqsaGreen.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AlAqsaGreen)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(top.productName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Text(
                                    text = "${top.totalPiecesSold} قطعة (${String.format(Locale.US, "%.1f", top.totalRevenue)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AlAqsaGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stagnant Products Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المنتجات الراكدة (لم تباع في آخر 30 يوماً)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFC62828))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (stagnantProducts.isEmpty()) {
                        Text("ممتاز! لا توجد منتجات راكدة في المكتبة.", color = AlAqsaGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        stagnantProducts.take(5).forEach { stag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stag.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("المتبقي: ${stag.stockPieces} قطعة", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}
