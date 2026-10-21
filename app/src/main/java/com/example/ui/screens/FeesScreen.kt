package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatchEntity
import com.example.data.FeeInvoiceEntity
import com.example.data.FeePaymentEntity
import com.example.data.StudentEntity
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Balances & Invoices, 1: Receipts Ledger, 2: Cohort Reminders
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) } // null = All, -1 = Unassigned, >0 = Batch ID
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PAID, OVERDUE

    // Modals state
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var studentForPreFilledPayment by remember { mutableStateOf<StudentEntity?>(null) }
    var paymentReceiptToView by remember { mutableStateOf<FeePaymentEntity?>(null) }
    var singleStudentReminderTarget by remember { mutableStateOf<Pair<StudentEntity, FeeInvoiceEntity>?>(null) }
    var batchForBulkReminders by remember { mutableStateOf<BatchEntity?>(null) }

    // Overall Financial Metrics
    val totalBilled = allInvoices.sumOf { it.totalFee }
    val totalCollected = allPayments.sumOf { it.amount }
    val totalPending = (totalBilled - totalCollected).coerceAtLeast(0.0)
    val realizationRate = if (totalBilled > 0) ((totalCollected / totalBilled) * 100).toInt() else 0

    // Filtered Invoices
    val filteredInvoices = allInvoices.filter { invoice ->
        val student = allStudents.find { it.id == invoice.studentId }
        val matchesSearch = student?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                student?.studentCode?.contains(searchQuery, ignoreCase = true) == true ||
                invoice.invoiceNumber.contains(searchQuery, ignoreCase = true)

        val matchesBatch = when (selectedBatchFilter) {
            null -> true
            -1L -> student?.assignedBatchId == null || student.assignedBatchId == 0L
            else -> student?.assignedBatchId == selectedBatchFilter
        }

        val matchesStatus = when (statusFilter) {
            "PENDING" -> invoice.balanceAmount > 0
            "PAID" -> invoice.balanceAmount == 0.0 || invoice.status == "PAID"
            "OVERDUE" -> invoice.balanceAmount > 0 // in real system compare dates
            else -> true
        }

        matchesSearch && matchesBatch && matchesStatus
    }

    // Filtered Payments
    val filteredPayments = allPayments.filter { payment ->
        val student = allStudents.find { it.id == payment.studentId }
        student?.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                student?.studentCode?.contains(searchQuery, ignoreCase = true) == true ||
                payment.receiptNumber.contains(searchQuery, ignoreCase = true) ||
                payment.paymentMethod.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == "ADMIN") {
                FloatingActionButton(
                    onClick = {
                        studentForPreFilledPayment = null
                        showRecordPaymentDialog = true
                    },
                    containerColor = GoldAccent,
                    contentColor = NavyPrimary,
                    modifier = Modifier.testTag("record_payment_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Record Payment")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fees & Balances Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Record payments, track pending balances, & dispatch batch reminders",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (currentUser.role == "ADMIN") {
                    Button(
                        onClick = {
                            studentForPreFilledPayment = null
                            showRecordPaymentDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("record_payment_button")
                    ) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payment", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Total Invoiced",
                    value = "₹${String.format(Locale.getDefault(), "%,.0f", totalBilled)}",
                    icon = Icons.Default.CurrencyRupee,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "${allInvoices.size} Invoices"
                )
                StatsCard(
                    title = "Collected",
                    value = "₹${String.format(Locale.getDefault(), "%,.0f", totalCollected)}",
                    icon = Icons.Default.CurrencyRupee,
                    iconBgColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    subText = "$realizationRate% Realized"
                )
                StatsCard(
                    title = "Pending Dues",
                    value = "₹${String.format(Locale.getDefault(), "%,.0f", totalPending)}",
                    icon = Icons.Default.CurrencyRupee,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "${allInvoices.count { it.balanceAmount > 0 }} Students"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student, student code, or invoice #", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fee_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Workspace Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Student Balances (${allInvoices.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_student_balances")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Receipts Ledger (${allPayments.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_receipts_ledger")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Batch Reminders (${allBatches.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_batch_reminders")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Filters for Tab 0: Student Balances
            if (selectedTabIndex == 0) {
                // Batch filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedBatchFilter = null }
                            .testTag("fee_filter_all_batches"),
                        color = if (selectedBatchFilter == null) NavyPrimary else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = if (selectedBatchFilter == null) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = "All Cohorts",
                            color = if (selectedBatchFilter == null) Color.White else NavyPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    allBatches.forEach { b ->
                        val isSel = selectedBatchFilter == b.id
                        val count = allInvoices.count { inv ->
                            val st = allStudents.find { it.id == inv.studentId }
                            st?.assignedBatchId == b.id
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedBatchFilter = b.id }
                                .testTag("fee_filter_batch_${b.id}"),
                            color = if (isSel) NavyPrimary else Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "${b.batchName} ($count)",
                                color = if (isSel) Color.White else NavyPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status chips (All, Pending, Paid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL" to "All Dues", "PENDING" to "Pending Dues (${allInvoices.count { it.balanceAmount > 0 }})", "PAID" to "Fully Settled (${allInvoices.count { it.balanceAmount == 0.0 }})").forEach { (key, label) ->
                        val isSel = statusFilter == key
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { statusFilter = key }
                                .testTag("fee_status_$key"),
                            color = if (isSel) NavySecondary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFF475569),
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: STUDENT BALANCES & INVOICES
                    if (filteredInvoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No fee records match the criteria", color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredInvoices, key = { it.id }) { invoice ->
                                val student = allStudents.find { it.id == invoice.studentId }
                                val batch = allBatches.find { it.id == student?.assignedBatchId }
                                val paidFraction = if (invoice.totalFee > 0) (invoice.paidAmount / invoice.totalFee).toFloat().coerceIn(0f, 1f) else 0f
                                val paidPct = (paidFraction * 100).toInt()
                                val isPending = invoice.balanceAmount > 0

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fee_card_${invoice.id}"),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header Row: Avatar, Name, Code, Cohort, Status Badge
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(NavySecondary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = (student?.fullName ?: "ST").take(2).uppercase(),
                                                    color = GoldAccent,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student?.fullName ?: "Student #${invoice.studentId}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = NavyPrimary
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = student?.studentCode ?: "",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text("•", fontSize = 11.sp, color = Color.Gray)
                                                    Text(
                                                        text = batch?.batchName ?: "Unassigned Batch",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (batch != null) Color(0xFF4F46E5) else Color(0xFFB91C1C)
                                                    )
                                                }
                                            }

                                            StatusBadge(status = invoice.status)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Financial Figures Grid
                                        Surface(
                                            color = Color(0xFFF8FAFC),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("TOTAL COURSE FEE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                    Text("₹${String.format(Locale.getDefault(), "%,.0f", invoice.totalFee)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                                                }

                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("PAID AMOUNT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                    Text("₹${String.format(Locale.getDefault(), "%,.0f", invoice.paidAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("PENDING BALANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isPending) BurgundyAccent else Color.Gray)
                                                    Text(
                                                        text = "₹${String.format(Locale.getDefault(), "%,.0f", invoice.balanceAmount)}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPending) BurgundyAccent else Color(0xFF16A34A)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Payment Realization Progress Bar
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Fee Clearance: $paidPct%", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                Text("Due: ${invoice.dueDate}", fontSize = 10.sp, color = if (isPending) BurgundyAccent else Color.Gray, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFFE2E8F0))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(fraction = paidFraction)
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(if (paidFraction >= 1f) Color(0xFF16A34A) else GoldAccent)
                                                )
                                            }
                                        }

                                        // Guardian & Mobile Contact
                                        student?.let { st ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Guardian: ${st.parentName} (📞 ${st.parentMobile})",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = invoice.invoiceNumber,
                                                    fontSize = 10.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (isPending && student != null) {
                                                    // Automated Payment Reminder Button
                                                    Button(
                                                        onClick = {
                                                            singleStudentReminderTarget = student to invoice
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.testTag("send_reminder_${student.id}")
                                                    ) {
                                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Send Reminder", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (currentUser.role == "ADMIN" && isPending && student != null) {
                                                    // Record Payment Shortcut
                                                    OutlinedButton(
                                                        onClick = {
                                                            studentForPreFilledPayment = student
                                                            showRecordPaymentDialog = true
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.testTag("record_payment_for_${student.id}")
                                                    ) {
                                                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Record Payment", fontSize = 11.sp)
                                                    }
                                                }
                                            }

                                            // Quick copy balance info
                                            IconButton(
                                                onClick = {
                                                    val info = "${student?.fullName ?: "Student"} (${student?.studentCode ?: ""}) - Pending Due: ₹${String.format(Locale.getDefault(), "%,.0f", invoice.balanceAmount)} [Invoice: ${invoice.invoiceNumber}]"
                                                    clipboardManager.setText(AnnotatedString(info))
                                                    Toast.makeText(context, "Balance info copied", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Balance Info", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: PAYMENT RECEIPTS LEDGER
                    if (filteredPayments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No payment receipts recorded yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredPayments, key = { it.id }) { payment ->
                                val student = allStudents.find { it.id == payment.studentId }
                                val batch = allBatches.find { it.id == student?.assignedBatchId }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("payment_row_${payment.id}"),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student?.fullName ?: "Student #${payment.studentId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "${payment.receiptNumber} • ${payment.paymentDate} • ${payment.paymentMethod}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Cohort: ${batch?.batchName ?: "General"} • Received by: ${payment.receivedBy}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            if (payment.remarks.isNotBlank()) {
                                                Text(
                                                    text = "Note: ${payment.remarks}",
                                                    fontSize = 10.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${String.format(Locale.getDefault(), "%,.0f", payment.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF16A34A)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedButton(
                                                onClick = { paymentReceiptToView = payment },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.testTag("view_receipt_${payment.id}")
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Receipt", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: BATCH FEE OVERVIEW & AUTOMATED REMINDERS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allBatches, key = { it.id }) { batch ->
                            val studentsInBatch = allStudents.filter { it.assignedBatchId == batch.id }
                            val invoicesInBatch = allInvoices.filter { inv ->
                                studentsInBatch.any { it.id == inv.studentId }
                            }
                            val cohortBilled = invoicesInBatch.sumOf { it.totalFee }
                            val cohortCollected = invoicesInBatch.sumOf { it.paidAmount }
                            val cohortPending = invoicesInBatch.sumOf { it.balanceAmount }
                            val pendingStudentsCount = invoicesInBatch.count { it.balanceAmount > 0 }
                            val clearancePct = if (cohortBilled > 0) ((cohortCollected / cohortBilled) * 100).toInt() else 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("batch_fee_card_${batch.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = batch.batchName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "${batch.courseName} • Subject: ${batch.subject} • Faculty: ${batch.teacherName}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Surface(
                                            color = if (cohortPending > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (cohortPending > 0) "$pendingStudentsCount with Dues" else "All Clear",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cohortPending > 0) Color(0xFFB91C1C) else Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Metrics row
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("ENROLLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Text("${studentsInBatch.size} Students", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("COLLECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Text("₹${String.format(Locale.getDefault(), "%,.0f", cohortCollected)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("PENDING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BurgundyAccent)
                                                Text("₹${String.format(Locale.getDefault(), "%,.0f", cohortPending)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BurgundyAccent)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Progress bar
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Cohort Clearance Progress", fontSize = 10.sp, color = Color.Gray)
                                            Text("$clearancePct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFE2E8F0))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = (clearancePct / 100f).coerceIn(0f, 1f))
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(if (clearancePct >= 100) Color(0xFF16A34A) else GoldAccent)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action Button
                                    Button(
                                        onClick = { batchForBulkReminders = batch },
                                        enabled = pendingStudentsCount > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("generate_batch_reminders_${batch.id}")
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (pendingStudentsCount > 0) "Generate Batch Reminders ($pendingStudentsCount Students)" else "No Pending Balances in Cohort",
                                            color = NavyPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Record Payment Dialog
    if (showRecordPaymentDialog) {
        val preFilledStudent = studentForPreFilledPayment
        var selectedStudentId by remember { mutableStateOf<Long?>(preFilledStudent?.id ?: allStudents.firstOrNull()?.id) }
        val currentStudent = allStudents.find { it.id == selectedStudentId }
        val studentInvoice = allInvoices.find { it.studentId == selectedStudentId }
        val pendingBalance = studentInvoice?.balanceAmount ?: 0.0

        var amountStr by remember { mutableStateOf(if (pendingBalance > 0) (pendingBalance.coerceAtMost(35000.0)).toInt().toString() else "25000") }
        var paymentDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var paymentMethod by remember { mutableStateOf("UPI") }
        var remarks by remember { mutableStateOf("Tuition Installment") }
        var studentDropdownOpen by remember { mutableStateOf(false) }

        val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
        val remainingAfterPayment = (pendingBalance - enteredAmount).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = {
                Column {
                    Text("Record Tuition Payment", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 17.sp)
                    Text("Enter payment details to allocate against student invoice", fontSize = 11.sp, color = Color.Gray)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Student selection dropdown
                    ExposedDropdownMenuBox(
                        expanded = studentDropdownOpen,
                        onExpandedChange = { studentDropdownOpen = !studentDropdownOpen }
                    ) {
                        OutlinedTextField(
                            value = currentStudent?.let { "${it.fullName} (${it.studentCode}) • Due: ₹${String.format(Locale.getDefault(), "%,.0f", pendingBalance)}" } ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Student Name *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = studentDropdownOpen,
                            onDismissRequest = { studentDropdownOpen = false }
                        ) {
                            allStudents.forEach { s ->
                                val inv = allInvoices.find { it.studentId == s.id }
                                val bal = inv?.balanceAmount ?: 0.0
                                DropdownMenuItem(
                                    text = {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${s.fullName} (${s.studentCode})")
                                            Text("Due: ₹${String.format(Locale.getDefault(), "%,.0f", bal)}", color = BurgundyAccent, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        selectedStudentId = s.id
                                        amountStr = (bal.coerceAtMost(35000.0)).toInt().toString()
                                        studentDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Balance Breakdown Banner
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Due: ₹${String.format(Locale.getDefault(), "%,.0f", pendingBalance)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BurgundyAccent)
                            Text("After: ₹${String.format(Locale.getDefault(), "%,.0f", remainingAfterPayment)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }

                    // Quick Installment Buttons
                    if (pendingBalance > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { amountStr = pendingBalance.toInt().toString() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Full Clear", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = { amountStr = (pendingBalance * 0.5).toInt().toString() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("50% (₹${(pendingBalance * 0.5).toInt()})", fontSize = 10.sp)
                            }
                        }
                    }

                    // Amount Field
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) amountStr = it },
                        label = { Text("Amount Paid (INR ₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_amount_input"),
                        singleLine = true
                    )

                    // Payment Date
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("Payment Date (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Payment Mode Chips
                    Text("Payment Mode *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI", "Cash", "Bank Transfer", "Cheque").forEach { mode ->
                            val isSel = paymentMethod == mode
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = mode },
                                color = if (isSel) NavyPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) GoldAccent else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Remarks
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Transaction Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (selectedStudentId != null && amt > 0) {
                            viewModel.recordPayment(
                                studentId = selectedStudentId!!,
                                amount = amt,
                                paymentDate = paymentDate,
                                paymentMethod = paymentMethod,
                                remarks = remarks
                            )
                            showRecordPaymentDialog = false
                            Toast.makeText(context, "Payment of ₹${String.format(Locale.getDefault(), "%,.0f", amt)} recorded successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier.testTag("submit_record_payment_button")
                ) {
                    Text("Record & Generate Receipt", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRecordPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Single Student Automated Payment Reminder
    singleStudentReminderTarget?.let { (student, invoice) ->
        val batch = allBatches.find { it.id == student.assignedBatchId }
        val reminderMessage = viewModel.generateStudentFeeReminder(student, batch, invoice)

        AlertDialog(
            onDismissRequest = { singleStudentReminderTarget = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Automated Payment Reminder", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 17.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Prepared notice for ${student.fullName} (${batch?.batchName ?: "Cohort"}). Outstanding balance: ₹${String.format(Locale.getDefault(), "%,.0f", invoice.balanceAmount)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reminderMessage,
                            fontSize = 11.sp,
                            color = NavyPrimary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recipient: ${student.parentName} • 📞 ${student.parentMobile}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reminderMessage))
                        Toast.makeText(context, "Payment reminder copied to clipboard for ${student.fullName}!", Toast.LENGTH_SHORT).show()
                        singleStudentReminderTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier.testTag("copy_reminder_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Reminder (SMS / WhatsApp)", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { singleStudentReminderTarget = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Automated Batch Payment Reminders Generator
    batchForBulkReminders?.let { batch ->
        val studentsInBatch = allStudents.filter { it.assignedBatchId == batch.id }
        val invoicesWithDues = allInvoices.filter { inv ->
            studentsInBatch.any { it.id == inv.studentId } && inv.balanceAmount > 0
        }
        val totalPendingAmount = invoicesWithDues.sumOf { it.balanceAmount }
        var customNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { batchForBulkReminders = null },
            title = {
                Column {
                    Text("Batch Payment Reminders", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 17.sp)
                    Text("Cohort: ${batch.batchName} (${invoicesWithDues.size} Students with Outstanding Balances)", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    // Summary Banner
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("OUTSTANDING BATCH DUES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text("₹${String.format(Locale.getDefault(), "%,.0f", totalPendingAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                            Text("${invoicesWithDues.size} Students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("STUDENT ROSTER WITH PENDING BALANCES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Roster of students with pending balances
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(invoicesWithDues, key = { it.id }) { inv ->
                            val st = allStudents.find { it.id == inv.studentId }

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(st?.fullName ?: "Student", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                        Text("${st?.studentCode} • Guardian: ${st?.parentName} (${st?.parentMobile})", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${String.format(Locale.getDefault(), "%,.0f", inv.balanceAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyAccent)
                                        Text("Due: ${inv.dueDate}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Optional Custom broadcast note
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        label = { Text("Custom Broadcast Instruction (optional)") },
                        placeholder = { Text("e.g. Please clear dues before next mock test") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Copy Bulk Reminders
                    OutlinedButton(
                        onClick = {
                            val bulkText = buildString {
                                appendLine("=== BATCH FEE REMINDERS: ${batch.batchName} ===")
                                appendLine("Total Pending: ₹${String.format(Locale.getDefault(), "%,.0f", totalPendingAmount)} (${invoicesWithDues.size} Students)")
                                appendLine("UPI: frontcraft.pro@upi | Contact: 9820011999")
                                appendLine("-------------------------------------------------")
                                invoicesWithDues.forEachIndexed { i, inv ->
                                    val st = allStudents.find { it.id == inv.studentId }
                                    appendLine("${i + 1}. ${st?.fullName} (${st?.studentCode}): Pending ₹${String.format(Locale.getDefault(), "%,.0f", inv.balanceAmount)} | Contact: ${st?.parentMobile}")
                                }
                            }
                            clipboardManager.setText(AnnotatedString(bulkText))
                            Toast.makeText(context, "Batch reminders text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Copy Reminders", fontSize = 11.sp)
                    }

                    // Broadcast to Notice Board
                    Button(
                        onClick = {
                            viewModel.broadcastBatchFeeReminder(batch.id, customNote)
                            Toast.makeText(context, "Automated fee reminder broadcasted to Notice Board!", Toast.LENGTH_SHORT).show()
                            batchForBulkReminders = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier.testTag("broadcast_notice_button")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Broadcast Notice", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { batchForBulkReminders = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Official Payment Receipt Dialog
    paymentReceiptToView?.let { payment ->
        val student = allStudents.find { it.id == payment.studentId }
        val batch = allBatches.find { it.id == student?.assignedBatchId }

        AlertDialog(
            onDismissRequest = { paymentReceiptToView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official Payment Receipt", fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
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
                            Column {
                                Text("FRONTCRAFT PRO ACADEMY", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                Text("Institute Fee Payment Voucher", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                            }
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("PAID", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt: ${payment.receiptNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text("Date: ${payment.paymentDate}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Student: ${student?.fullName ?: "Student"} (${student?.studentCode ?: ""})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                Text("Cohort: ${batch?.batchName ?: "General Cohort"} • ${student?.courseName ?: ""}", fontSize = 11.sp, color = Color.Gray)
                                Text("Payment Mode: ${payment.paymentMethod}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Amount Realized", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%,.0f", payment.amount)}",
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Received By: ${payment.receivedBy}", fontSize = 10.sp, color = Color.Gray)
                        if (payment.remarks.isNotBlank()) {
                            Text("Remarks: ${payment.remarks}", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val receiptText = """
=== FRONTCRAFT PRO FEE RECEIPT ===
Receipt No: ${payment.receiptNumber}
Date: ${payment.paymentDate}
Student: ${student?.fullName} (${student?.studentCode})
Cohort: ${batch?.batchName ?: "General"}
Amount: ₹${String.format(Locale.getDefault(), "%,.0f", payment.amount)}
Mode: ${payment.paymentMethod}
Received By: ${payment.receivedBy}
Remarks: ${payment.remarks}
==================================
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(receiptText))
                        Toast.makeText(context, "Official receipt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        paymentReceiptToView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Receipt", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { paymentReceiptToView = null }) {
                    Text("Close")
                }
            }
        )
    }
}
