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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.util.CsvExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allDailyAttendance by viewModel.allDailyAttendance.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allExaminations by viewModel.allExaminations.collectAsState()
    val allExamResults by viewModel.allExamResults.collectAsState()

    val fileTimestamp = remember { SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date()) }

    var selectedBatchFilterId by remember { mutableStateOf<Long?>(null) } // null = All Batches
    var batchDropdownOpen by remember { mutableStateOf(false) }

    // Dialog preview state: Pair(fileName, csvContent)
    var previewExportData by remember { mutableStateOf<Pair<String, String>?>(null) }

    val filteredAttendance = if (selectedBatchFilterId == null) {
        allDailyAttendance
    } else {
        allDailyAttendance.filter { it.batchId == selectedBatchFilterId }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Administrative Reports & CSV Exporter",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Export Room database student records and daily attendance data into sanitized, shareable CSV spreadsheets.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Metrics Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Database Students",
                    value = "${allStudents.size}",
                    icon = Icons.Default.Groups,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "${allBatches.size} Cohorts"
                )
                StatsCard(
                    title = "Attendance Logs",
                    value = "${allDailyAttendance.size}",
                    icon = Icons.Default.CalendarToday,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Daily Records"
                )
                StatsCard(
                    title = "Evaluations",
                    value = "${allExamResults.size}",
                    icon = Icons.Default.Assessment,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Test Scores"
                )
            }
        }

        // CSV Injection Defense Notice
        item {
            Surface(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OWASP Sanitization Active: Cells starting with =, +, -, @ are escaped to protect against CSV formula injection when opened in Excel or Google Sheets.",
                        fontSize = 10.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // --- EXPORT MODULE 1: Student Records Directory CSV ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_card_students"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Student Records Directory (CSV)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                Text("${allStudents.size} Registered Students • Profiles & Contacts", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "16 Fields",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Includes Student ID, Code, Full Name, Contact Mobile, Email, Cohort Batch, Course Program, Admission Date, Guardian Name, Guardian Mobile, Address, Fee Quota, Paid Fee, and Balance Outstanding.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = CsvExporter.generateStudentsCsv(allStudents, allBatches, allInvoices)
                                previewExportData = Pair("frontcraft_students_$fileTimestamp.csv", csv)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_students_csv_button")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview CSV", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val csv = CsvExporter.generateStudentsCsv(allStudents, allBatches, allInvoices)
                                val fileName = "frontcraft_students_$fileTimestamp.csv"
                                val file = CsvExporter.saveAndShareCsv(
                                    context = context,
                                    fileName = fileName,
                                    csvContent = csv,
                                    shareTitle = "Share Student Records CSV"
                                )
                                if (file != null) {
                                    Toast.makeText(context, "Exported ${allStudents.size} student records to $fileName", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to create CSV file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("share_students_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export & Share CSV", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- EXPORT MODULE 2: Daily Student Attendance Register CSV ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_card_attendance"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Daily Attendance Register (CSV)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                Text("${filteredAttendance.size} Evaluated Logs • Daily Roll Calls", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Daily Records",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Exports individual daily attendance records: Attendance Date, Cohort, Subject, Student Code, Full Name, Contact Mobile, Status (PRESENT, ABSENT, LATE, LEAVE), Faculty Remarks, and Evaluator.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cohort Filter Carousel
                    Text("Filter Cohort for Attendance Export:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            Surface(
                                color = if (selectedBatchFilterId == null) NavyPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.clickable { selectedBatchFilterId = null }
                            ) {
                                Text(
                                    text = "All Batches Combined (${allDailyAttendance.size})",
                                    color = if (selectedBatchFilterId == null) Color.White else NavyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(allBatches, key = { it.id }) { b ->
                            val isSel = selectedBatchFilterId == b.id
                            val count = allDailyAttendance.count { it.batchId == b.id }
                            Surface(
                                color = if (isSel) NavyPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.clickable { selectedBatchFilterId = b.id }
                            ) {
                                Text(
                                    text = "${b.batchName} ($count)",
                                    color = if (isSel) Color.White else NavyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = CsvExporter.generateAttendanceCsv(filteredAttendance, allStudents, allBatches)
                                val bName = allBatches.find { it.id == selectedBatchFilterId }?.batchName?.replace(" ", "_") ?: "all_batches"
                                previewExportData = Pair("frontcraft_attendance_${bName}_$fileTimestamp.csv", csv)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_attendance_csv_button")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview CSV", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val csv = CsvExporter.generateAttendanceCsv(filteredAttendance, allStudents, allBatches)
                                val bName = allBatches.find { it.id == selectedBatchFilterId }?.batchName?.replace(" ", "_") ?: "all_batches"
                                val fileName = "frontcraft_attendance_${bName}_$fileTimestamp.csv"
                                val file = CsvExporter.saveAndShareCsv(
                                    context = context,
                                    fileName = fileName,
                                    csvContent = csv,
                                    shareTitle = "Share Daily Attendance CSV"
                                )
                                if (file != null) {
                                    Toast.makeText(context, "Exported ${filteredAttendance.size} attendance records to $fileName", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to create CSV file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("share_attendance_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export & Share CSV", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- EXPORT MODULE 3: Master Administrative & Academic Ledger CSV ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_card_master"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = BurgundyAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Master Administrative Ledger (CSV)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                                Text("Comprehensive Academic, Attendance & Financial Roster", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "All-In-One",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Institutional master audit file: combines student profiles, attendance frequency counts, attendance rate (%), total tests taken, average score (%), cumulative grade, and tuition balance outstanding.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = CsvExporter.generateMasterAdministrativeReportCsv(
                                    students = allStudents,
                                    batches = allBatches,
                                    dailyAttendance = allDailyAttendance,
                                    examResults = allExamResults,
                                    examinations = allExaminations,
                                    invoices = allInvoices
                                )
                                previewExportData = Pair("frontcraft_master_admin_ledger_$fileTimestamp.csv", csv)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_master_csv_button")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview CSV", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val csv = CsvExporter.generateMasterAdministrativeReportCsv(
                                    students = allStudents,
                                    batches = allBatches,
                                    dailyAttendance = allDailyAttendance,
                                    examResults = allExamResults,
                                    examinations = allExaminations,
                                    invoices = allInvoices
                                )
                                val fileName = "frontcraft_master_admin_ledger_$fileTimestamp.csv"
                                val file = CsvExporter.saveAndShareCsv(
                                    context = context,
                                    fileName = fileName,
                                    csvContent = csv,
                                    shareTitle = "Share Master Administrative Ledger CSV"
                                )
                                if (file != null) {
                                    Toast.makeText(context, "Exported master roster to $fileName", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to create CSV file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("share_master_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export & Share CSV", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal: CSV Preview & Share Dialog
    previewExportData?.let { (fileName, csvText) ->
        val lines = csvText.lines().filter { it.isNotBlank() }
        val rowCount = if (lines.isNotEmpty()) lines.size - 1 else 0
        val byteSize = csvText.toByteArray().size

        AlertDialog(
            onDismissRequest = { previewExportData = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CSV File Preview", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 16.sp)
                        Text("$fileName • $rowCount data rows • ${byteSize / 1024 + 1} KB", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { previewExportData = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Data preview (first 10 records shown below):",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            items(lines.take(12)) { line ->
                                Text(
                                    text = line,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(csvText))
                            Toast.makeText(context, "CSV raw text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Raw Text", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val file = CsvExporter.saveAndShareCsv(
                                context = context,
                                fileName = fileName,
                                csvContent = csvText,
                                shareTitle = "Share $fileName"
                            )
                            if (file != null) {
                                Toast.makeText(context, "Exported $fileName", Toast.LENGTH_SHORT).show()
                            }
                            previewExportData = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share File", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { previewExportData = null }) {
                    Text("Close")
                }
            }
        )
    }
}
