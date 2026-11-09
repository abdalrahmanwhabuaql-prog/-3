package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.AlAqsaGold
import com.example.ui.theme.AlAqsaGreen
import com.example.ui.viewmodel.PosViewModel

@Composable
fun BackupSettingsScreen(
    viewModel: PosViewModel
) {
    val context = LocalContext.current
    var exportedJsonText by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Identity Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.al_aqsa_logo),
                            contentDescription = "شعار مكتبة الأقصى الرسمي",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AL-AQSA POS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                    Text(
                        text = "نظام كاشير وإدارة مكتبة الأقصى – عبدالرحمن",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AlAqsaGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "الإصدار 1.0 • قاعدة بيانات محلية SQLite Room",
                            color = AlAqsaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Backup & Restore Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AlAqsaGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "النسخ الاحتياطي وحماية البيانات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتم حفظ جميع المنتجات والمبيعات والمشتريات وحركات الصندوق محلياً على هاتفك دون الحاجة لإنترنت. يمكنك تصدير نسخة احتياطية بصيغة JSON لمشاركتها أو استعادتها في أي وقت.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Button
                    Button(
                        onClick = {
                            viewModel.exportBackup { json ->
                                exportedJsonText = json
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير نسخة احتياطية (JSON)")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Import Button
                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = AlAqsaGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد واستعادة نسخة احتياطية")
                    }
                }
            }
        }

        // System Features & Principles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AlAqsaGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مميزات النظام التقنية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureBullet(title = "العمل دون إنترنت بالكامل", desc = "كافة عمليات البيع والمخزون والحسابات تعمل بسرعة فائقة دون اتصال.")
                    FeatureBullet(title = "تحويل الوحدات (كرتونة / دستة / قطعة)", desc = "شراء بالكرتونة والبيع بالقطعة مع حساب دقيق للتكلفة والربح.")
                    FeatureBullet(title = "لا يشترط وجود طابعة", desc = "تسجيل فوري للمبيعات مع حفظ رقم الفاتورة والعميل دون الحاجة لطباعة ورقية.")
                    FeatureBullet(title = "حفظ سجلات المبيعات السابقة", desc = "تعديل أسعار أو تفاصيل المنتجات لا يمس السجلات التاريخية وفواتير البيع.")
                    FeatureBullet(title = "فصل السحوبات الشخصية", desc = "سحوبات عبدالرحمن مسجلة بشكل منفصل عن مصروفات تشغيل المكتبة.")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Export Result Dialog
    exportedJsonText?.let { json ->
        Dialog(onDismissRequest = { exportedJsonText = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "تم إنشاء النسخة الاحتياطية بنجاح!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "حجم البيانات: ${json.length} حرف. يمكنك نسخ النص أو مشاركته لحفظه في Drive أو WhatsApp:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = json.take(300) + if (json.length > 300) "..." else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AlAqsa_Backup", json)
                                clipboard.setPrimaryClip(clip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ")
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - مكتبة الأقصى")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة النسخة الاحتياطية"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGold, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { exportedJsonText = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "استيراد واستعادة نسخة احتياطية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlAqsaGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الصق نص الـ JSON الخاص بالنسخة الاحتياطية هنا:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("الصق بيانات JSON هنا...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showImportDialog = false }) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (importJsonInput.isNotBlank()) {
                                    viewModel.importBackup(importJsonInput.trim())
                                    showImportDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlAqsaGreen),
                            enabled = importJsonInput.isNotBlank()
                        ) {
                            Text("تأكيد الاستيراد")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureBullet(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(AlAqsaGold, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 14.dp))
    }
}
