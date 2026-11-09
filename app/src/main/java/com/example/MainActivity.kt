package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.BackupSettingsScreen
import com.example.ui.screens.CashDrawerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosCashierScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.AlAqsaDarkGreen
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.UiEvent
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Mandatory Arabic RTL Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AlAqsaPosApp(viewModel)
                }
            }
        }
    }
}

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlAqsaPosApp(viewModel: PosViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.selectedTab.collectAsState()

    var showSettingsScreen by remember { mutableStateOf(false) }
    var saleSuccessInfo by remember { mutableStateOf<Pair<String, Double>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowSaleSuccess -> {
                    saleSuccessInfo = Pair(event.invoiceNumber, event.total)
                }
            }
        }
    }

    val navItems = listOf(
        NavItem("الرئيسية", Icons.Default.Dashboard, 0),
        NavItem("الكاشير", Icons.Default.PointOfSale, 1),
        NavItem("المخزون", Icons.Default.Inventory2, 2),
        NavItem("المشتريات", Icons.Default.AddBusiness, 3),
        NavItem("الصندوق", Icons.Default.AccountBalanceWallet, 4),
        NavItem("التقارير", Icons.Default.Assessment, 5)
    )

    Scaffold(
        topBar = {
            Surface(
                color = AlAqsaDarkGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.al_aqsa_logo),
                                contentDescription = "شعار مكتبة الأقصى الرسمي",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(3.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AL-AQSA POS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "مكتبة الأقصى – عبدالرحمن",
                                color = AlAqsaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = { showSettingsScreen = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "النسخ الاحتياطي والإعدادات",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                navItems.forEach { item ->
                    val isSelected = currentTab == item.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectedTab.value = item.index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AlAqsaGreen,
                            indicatorColor = AlAqsaGreen,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToPos = { viewModel.selectedTab.value = 1 },
                    onNavigateToInventory = { viewModel.selectedTab.value = 2 },
                    onNavigateToPurchases = { viewModel.selectedTab.value = 3 },
                    onNavigateToCash = { viewModel.selectedTab.value = 4 }
                )
                1 -> PosCashierScreen(viewModel = viewModel)
                2 -> InventoryScreen(viewModel = viewModel)
                3 -> PurchasesScreen(viewModel = viewModel)
                4 -> CashDrawerScreen(viewModel = viewModel)
                5 -> ReportsScreen(viewModel = viewModel)
            }
        }
    }

    // Settings & Backup Full Dialog
    if (showSettingsScreen) {
        Dialog(
            onDismissRequest = { showSettingsScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    Surface(color = AlAqsaDarkGreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "النسخ الاحتياطي وإعدادات المكتبة",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { showSettingsScreen = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                            }
                        }
                    }
                }
            ) { pad ->
                Box(modifier = Modifier.padding(pad)) {
                    BackupSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Sale Success Celebration Dialog
    saleSuccessInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { saleSuccessInfo = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AlAqsaGreen,
                    modifier = Modifier.size(52.dp)
                )
            },
            title = {
                Text(
                    text = "تم تسجيل البيع بنجاح!",
                    fontWeight = FontWeight.Bold,
                    color = AlAqsaGreen
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "رقم الفاتورة: ${info.first}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المبلغ الإجمالي: ${String.format(Locale.US, "%.2f", info.second)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تم تحديث المخزون وحفظ المبلغ بالصندوق دون الحاجة لطباعة ورقية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { saleSuccessInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("متابعة البيع", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
