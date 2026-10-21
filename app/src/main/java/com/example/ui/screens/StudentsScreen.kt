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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.ExaminationEntity
import com.example.data.ExamResultEntity
import com.example.data.StudentEntity
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allExaminations by viewModel.allExaminations.collectAsState()
    val allExamResults by viewModel.allExamResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) } // null = All, -1 = Unassigned, >0 = Batch ID
    var showRegisterDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var studentForReassignBatch by remember { mutableStateOf<StudentEntity?>(null) }
    var studentForIdCard by remember { mutableStateOf<StudentEntity?>(null) }
    var studentForPerformance by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }

    // Analytics
    val totalStudentsCount = allStudents.size
    val assignedCount = allStudents.count { it.assignedBatchId != null && it.assignedBatchId != 0L }
    val unassignedCount = totalStudentsCount - assignedCount
    val activeBatchesCount = allBatches.size

    val filteredStudents = allStudents.filter { st ->
        val matchesQuery = st.fullName.contains(searchQuery, ignoreCase = true) ||
                st.studentCode.contains(searchQuery, ignoreCase = true) ||
                st.mobile.contains(searchQuery) ||
                (st.email?.contains(searchQuery, ignoreCase = true) == true) ||
                st.parentName.contains(searchQuery, ignoreCase = true) ||
                st.parentMobile.contains(searchQuery)

        val matchesBatch = when (selectedBatchFilter) {
            null -> true
            -1L -> st.assignedBatchId == null || st.assignedBatchId == 0L
            else -> st.assignedBatchId == selectedBatchFilter
        }
        matchesQuery && matchesBatch
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == "ADMIN") {
                FloatingActionButton(
                    onClick = { showRegisterDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyPrimary,
                    modifier = Modifier.testTag("add_student_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Register New Student")
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
                        text = "Student Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Register students with personal details & assign to existing batches",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (currentUser.role == "ADMIN") {
                    Button(
                        onClick = { showRegisterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_student_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Register Student", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Analytics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Total Students",
                    value = "$totalStudentsCount",
                    icon = Icons.Default.Groups,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "Enrolled Roster"
                )
                StatsCard(
                    title = "Assigned to Batches",
                    value = "$assignedCount",
                    icon = Icons.Default.Class,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "In Active Cohorts"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Unassigned Students",
                    value = "$unassignedCount",
                    icon = Icons.Default.Person,
                    iconBgColor = if (unassignedCount > 0) BurgundyAccent else Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    subText = if (unassignedCount > 0) "Needs Batch Assignment" else "All Assigned"
                )
                StatsCard(
                    title = "Active Batches",
                    value = "$activeBatchesCount",
                    icon = Icons.Default.School,
                    iconBgColor = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    subText = "Available Cohorts"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_student_input"),
                placeholder = { Text("Search by name, roll code, phone, email, or guardian...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Batch filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedBatchFilter = null },
                    color = if (selectedBatchFilter == null) NavyPrimary else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = if (selectedBatchFilter == null) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "All ($totalStudentsCount)",
                        color = if (selectedBatchFilter == null) Color.White else NavyPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedBatchFilter = -1L },
                    color = if (selectedBatchFilter == -1L) BurgundyAccent else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = if (selectedBatchFilter == -1L) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "Unassigned ($unassignedCount)",
                        color = if (selectedBatchFilter == -1L) Color.White else Color(0xFFB91C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                allBatches.forEach { b ->
                    val isSel = selectedBatchFilter == b.id
                    val bCount = allStudents.count { it.assignedBatchId == b.id }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedBatchFilter = b.id },
                        color = if (isSel) NavyPrimary else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = "${b.batchName} ($bCount)",
                            color = if (isSel) Color.White else NavyPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Students List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No students found", color = Color.Gray, fontWeight = FontWeight.Medium)
                        if (currentUser.role == "ADMIN") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { showRegisterDialog = true }) {
                                Text("Register First Student")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val batch = allBatches.find { it.id == student.assignedBatchId }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_row_${student.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Avatar, Name, Code, and Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(NavySecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.fullName.take(2).uppercase(),
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student.studentCode,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Gray
                                            )
                                            Text("•", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = "Admitted: ${student.admissionDate}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    StatusBadge(status = student.status)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Batch Assignment Info Banner
                                Surface(
                                    color = if (batch != null) Color(0xFFEEF2FF) else Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (batch != null) Color(0xFFC7D2FE) else Color(0xFFFCA5A5)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (batch != null) "Assigned Batch: ${batch.batchName}" else "Unassigned Batch",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (batch != null) Color(0xFF3730A3) else Color(0xFF991B1B)
                                            )
                                            Text(
                                                text = if (batch != null) "Subject: ${batch.subject} • Faculty: ${batch.teacherName} • ${batch.scheduleTime}"
                                                else "This student has not been assigned to a cohort yet",
                                                fontSize = 10.sp,
                                                color = if (batch != null) Color(0xFF4F46E5) else Color(0xFFB91C1C)
                                            )
                                        }

                                        if (currentUser.role == "ADMIN") {
                                            OutlinedButton(
                                                onClick = { studentForReassignBatch = student },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.testTag("reassign_batch_student_${student.id}")
                                            ) {
                                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (batch != null) "Transfer" else "Assign",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Personal Details & Guardian Details
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "PERSONAL & GUARDIAN DETAILS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Student phone
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(student.mobile))
                                                        Toast.makeText(context, "Copied phone: ${student.mobile}", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .testTag("student_phone_${student.id}")
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(student.mobile, fontSize = 11.sp, color = NavyPrimary)
                                            }

                                            // Email
                                            if (!student.email.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(student.email))
                                                            Toast.makeText(context, "Copied email: ${student.email}", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Icon(Icons.Default.Email, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(student.email, fontSize = 11.sp, color = NavyPrimary)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Guardian details
                                        Text(
                                            text = "Guardian: ${student.parentName} • 📞 ${student.parentMobile}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )

                                        if (!student.address.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = student.address,
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        val invoice = allInvoices.find { it.studentId == student.id }
                                        val hasPendingFee = (invoice?.balanceAmount ?: 0.0) > 0

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Fee: ₹${String.format(Locale.getDefault(), "%,.0f", student.totalCourseFee)} • Paid: ₹${String.format(Locale.getDefault(), "%,.0f", invoice?.paidAmount ?: 0.0)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569)
                                            )
                                            if (invoice != null) {
                                                Text(
                                                    text = if (hasPendingFee) "Due: ₹${String.format(Locale.getDefault(), "%,.0f", invoice.balanceAmount)}" else "Fee Cleared",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasPendingFee) BurgundyAccent else Color(0xFF16A34A)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Historical Performance Mini Banner
                                val studentResults = allExamResults.filter { it.studentId == student.id }
                                if (studentResults.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val totalMax = studentResults.mapNotNull { r -> allExaminations.find { it.id == r.examId }?.maxMarks }.sum()
                                    val totalObtained = studentResults.sumOf { it.marksObtained }
                                    val avgPct = if (totalMax > 0) ((totalObtained / totalMax) * 100).toInt() else 0
                                    val overallGrade = viewModel.calculateGrade(totalObtained, totalMax)
                                    val passCount = studentResults.count { it.status == "PASS" }

                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { studentForPerformance = student }
                                            .testTag("student_perf_banner_${student.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Assessment, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Performance: ${studentResults.size} Tests • $avgPct% Avg ($passCount Passed)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyPrimary
                                                )
                                            }
                                            Surface(
                                                color = when {
                                                    overallGrade.startsWith("A") -> Color(0xFFDCFCE7)
                                                    overallGrade.startsWith("B") -> Color(0xFFE0E7FF)
                                                    else -> Color(0xFFFEF3C7)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Grade $overallGrade",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        overallGrade.startsWith("A") -> Color(0xFF15803D)
                                                        overallGrade.startsWith("B") -> Color(0xFF3730A3)
                                                        else -> Color(0xFFB45309)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons Row: Exam History, Generate ID Card, Edit, Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { studentForPerformance = student },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("student_performance_${student.id}")
                                        ) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Exam History", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        }

                                        OutlinedButton(
                                            onClick = { studentForIdCard = student },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("generate_id_card_${student.id}")
                                        ) {
                                            Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ID Card", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        }
                                    }

                                    if (currentUser.role == "ADMIN") {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { studentToEdit = student },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("edit_student_${student.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Student", modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Edit", fontSize = 11.sp)
                                            }

                                            IconButton(
                                                onClick = { studentToDelete = student },
                                                modifier = Modifier.testTag("delete_student_${student.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Register New Student or Edit Details
    if (showRegisterDialog) {
        RegisterOrEditStudentDialog(
            existingStudent = null,
            allBatches = allBatches,
            allStudents = allStudents,
            onDismiss = { showRegisterDialog = false },
            onSave = { name, mobile, email, pName, pMobile, addr, date, batchId, course, fee ->
                viewModel.enrollStudent(
                    fullName = name,
                    mobile = mobile,
                    email = email,
                    parentName = pName,
                    parentMobile = pMobile,
                    address = addr,
                    admissionDate = date,
                    assignedBatchId = batchId,
                    courseName = course,
                    totalCourseFee = fee
                )
                showRegisterDialog = false
                val assignedBatch = allBatches.find { it.id == batchId }
                val batchMsg = if (assignedBatch != null) "assigned to ${assignedBatch.batchName}" else "without initial batch"
                Toast.makeText(context, "Student $name registered and $batchMsg!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    studentToEdit?.let { student ->
        RegisterOrEditStudentDialog(
            existingStudent = student,
            allBatches = allBatches,
            allStudents = allStudents,
            onDismiss = { studentToEdit = null },
            onSave = { name, mobile, email, pName, pMobile, addr, date, batchId, course, fee ->
                viewModel.updateStudent(
                    student.copy(
                        fullName = name,
                        mobile = mobile,
                        email = email,
                        parentName = pName,
                        parentMobile = pMobile,
                        address = addr,
                        admissionDate = date,
                        assignedBatchId = batchId,
                        courseName = course,
                        totalCourseFee = fee
                    )
                )
                studentToEdit = null
                Toast.makeText(context, "Student details updated for $name!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Reassign Batch Dialog
    studentForReassignBatch?.let { student ->
        ReassignBatchDialog(
            student = student,
            allBatches = allBatches,
            allStudents = allStudents,
            onDismiss = { studentForReassignBatch = null },
            onSave = { newBatchId ->
                if (newBatchId == null || newBatchId == 0L) {
                    viewModel.removeStudentFromBatch(student.id)
                    Toast.makeText(context, "${student.fullName} unassigned from batch", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.assignStudentToBatch(student.id, newBatchId)
                    val bName = allBatches.find { it.id == newBatchId }?.batchName ?: "Batch"
                    Toast.makeText(context, "${student.fullName} assigned to $bName!", Toast.LENGTH_SHORT).show()
                }
                studentForReassignBatch = null
            }
        )
    }

    // Modal: Digital ID Card Dialog
    studentForIdCard?.let { student ->
        val batch = allBatches.find { it.id == student.assignedBatchId }

        AlertDialog(
            onDismissRequest = { studentForIdCard = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Digital Identity Card", fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            },
            text = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_id_card_view"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyPrimary)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FRONTCRAFT PRO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("STUDENT IDENTITY CARD • 2026-2027", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NavySecondary)
                                .border(2.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                color = GoldAccent,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(student.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Surface(
                            color = BurgundyAccent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(student.studentCode, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Info lines
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Batch:", fontSize = 11.sp, color = Color.Gray)
                                Text(batch?.batchName ?: "General / Unassigned", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subject / Stream:", fontSize = 11.sp, color = Color.Gray)
                                Text(batch?.subject ?: "All Subjects", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Phone:", fontSize = 11.sp, color = Color.Gray)
                                Text(student.mobile, fontSize = 11.sp, color = NavyPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Validity:", fontSize = 11.sp, color = Color.Gray)
                                Text("Active Thru 2027", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Simulated barcode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("||||| ||| ||||||| ||| ||||| ||||", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cardData = "FRONTCRAFT PRO ID: ${student.fullName} (${student.studentCode}) - Batch: ${batch?.batchName ?: "Unassigned"}"
                        clipboardManager.setText(AnnotatedString(cardData))
                        Toast.makeText(context, "Digital ID Card details copied & ready for download!", Toast.LENGTH_SHORT).show()
                        studentForIdCard = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Card", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentForIdCard = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Delete Student Confirmation
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Remove Student", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = { Text("Are you sure you want to remove ${student.fullName} (${student.studentCode}) from the institute database?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student.id, student.fullName)
                        studentToDelete = null
                        Toast.makeText(context, "Student removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Historical Performance Database Dialog Linked to Student Profile
    studentForPerformance?.let { student ->
        val batch = allBatches.find { it.id == student.assignedBatchId }
        val studentResults = allExamResults.filter { it.studentId == student.id }

        StudentPerformanceHistoryDialog(
            student = student,
            batch = batch,
            results = studentResults,
            allExaminations = allExaminations,
            viewModel = viewModel,
            onDismiss = { studentForPerformance = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOrEditStudentDialog(
    existingStudent: StudentEntity?,
    allBatches: List<BatchEntity>,
    allStudents: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        mobile: String,
        email: String?,
        parentName: String,
        parentMobile: String,
        address: String?,
        admissionDate: String,
        assignedBatchId: Long?,
        courseName: String,
        totalCourseFee: Double
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(existingStudent?.fullName ?: "") }
    var mobile by remember { mutableStateOf(existingStudent?.mobile ?: "") }
    var email by remember { mutableStateOf(existingStudent?.email ?: "") }
    var parentName by remember { mutableStateOf(existingStudent?.parentName ?: "") }
    var parentMobile by remember { mutableStateOf(existingStudent?.parentMobile ?: "") }
    var address by remember { mutableStateOf(existingStudent?.address ?: "") }
    var admissionDate by remember { mutableStateOf(existingStudent?.admissionDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var assignedBatchId by remember { mutableStateOf<Long?>(existingStudent?.assignedBatchId ?: allBatches.firstOrNull()?.id) }
    var courseName by remember { mutableStateOf(existingStudent?.courseName ?: allBatches.firstOrNull()?.courseName ?: "IIT-JEE Target 2026") }
    var feeStr by remember { mutableStateOf(existingStudent?.totalCourseFee?.toInt()?.toString() ?: "90000") }
    var batchDropdownOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedBatch = allBatches.find { it.id == assignedBatchId }
    val enrolledInSelected = if (selectedBatch != null) allStudents.count { it.assignedBatchId == selectedBatch.id && it.id != existingStudent?.id } else 0
    val isBatchFull = selectedBatch != null && enrolledInSelected >= selectedBatch.capacity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingStudent != null) "Edit Student Details" else "Register New Student",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section Header: Personal Details
                item {
                    Text(
                        text = "1. STUDENT PERSONAL DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                }

                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMessage = null },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Aarav Mehta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it; errorMessage = null },
                        label = { Text("Student Mobile *") },
                        placeholder = { Text("10-digit mobile number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_mobile"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("student@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_email"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        placeholder = { Text("Street, City, Postal Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_address"),
                        singleLine = true
                    )
                }

                // Section Header: Guardian Details
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. PARENT / GUARDIAN INFORMATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                }

                item {
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it; errorMessage = null },
                        label = { Text("Guardian / Parent Name *") },
                        placeholder = { Text("e.g. Sunil Mehta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_guardian_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = parentMobile,
                        onValueChange = { parentMobile = it; errorMessage = null },
                        label = { Text("Guardian Mobile Contact *") },
                        placeholder = { Text("Emergency contact number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_guardian_mobile"),
                        singleLine = true
                    )
                }

                // Section Header: Academic & Batch Assignment
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. BATCH & ACADEMIC PROGRAM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                }

                // Existing Batch Dropdown
                item {
                    Column {
                        ExposedDropdownMenuBox(
                            expanded = batchDropdownOpen,
                            onExpandedChange = { batchDropdownOpen = !batchDropdownOpen }
                        ) {
                            OutlinedTextField(
                                value = if (selectedBatch != null) "${selectedBatch.batchName} (${selectedBatch.subject})" else "Unassigned (Allocate Later)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assign to Existing Batch *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("select_student_batch")
                            )

                            ExposedDropdownMenu(
                                expanded = batchDropdownOpen,
                                onDismissRequest = { batchDropdownOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Keep Unassigned (Allocate Later)") },
                                    onClick = {
                                        assignedBatchId = null
                                        batchDropdownOpen = false
                                    }
                                )

                                allBatches.forEach { b ->
                                    val bEnrolled = allStudents.count { it.assignedBatchId == b.id }
                                    val isFull = bEnrolled >= b.capacity
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "${b.batchName} • ${b.subject}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Faculty: ${b.teacherName} • Seats: $bEnrolled / ${b.capacity} ${if (isFull) "(FULL)" else ""}",
                                                    fontSize = 11.sp,
                                                    color = if (isFull) Color(0xFFDC2626) else Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            assignedBatchId = b.id
                                            courseName = b.courseName
                                            batchDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isBatchFull) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Warning: Selected batch is at full capacity (${selectedBatch?.capacity} seats).",
                                        color = Color(0xFF991B1B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Course Name
                item {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Academic Course / Program *") },
                        placeholder = { Text("e.g. IIT-JEE Target 2026") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_course_name"),
                        singleLine = true
                    )
                }

                // Total Fee
                item {
                    Column {
                        OutlinedTextField(
                            value = feeStr,
                            onValueChange = { if (it.all { c -> c.isDigit() }) feeStr = it },
                            label = { Text("Total Course Fee (₹) *") },
                            placeholder = { Text("e.g. 90000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_course_fee"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(45000, 60000, 75000, 90000, 120000).forEach { amt ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { feeStr = amt.toString() },
                                    color = if (feeStr == amt.toString()) GoldAccent else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "₹${amt / 1000}k",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (feeStr == amt.toString()) NavyPrimary else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Admission Date
                item {
                    OutlinedTextField(
                        value = admissionDate,
                        onValueChange = { admissionDate = it },
                        label = { Text("Admission Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admission_date"),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeStr.toDoubleOrNull() ?: 0.0
                    if (fullName.isBlank() || mobile.isBlank() || parentName.isBlank() || parentMobile.isBlank() || fee <= 0) {
                        errorMessage = "Please enter student name, mobile, guardian details, and course fee."
                        return@Button
                    }
                    onSave(
                        fullName.trim(),
                        mobile.trim(),
                        email.trim().takeIf { it.isNotBlank() },
                        parentName.trim(),
                        parentMobile.trim(),
                        address.trim().takeIf { it.isNotBlank() },
                        admissionDate.trim(),
                        assignedBatchId,
                        courseName.trim(),
                        fee
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(
                    text = if (existingStudent != null) "Update Student" else "Register Student",
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReassignBatchDialog(
    student: StudentEntity,
    allBatches: List<BatchEntity>,
    allStudents: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (newBatchId: Long?) -> Unit
) {
    var selectedBatchId by remember { mutableStateOf<Long?>(student.assignedBatchId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assign / Transfer Batch",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${student.fullName} (${student.studentCode})",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Select an existing batch to allocate this student:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option to unassign
                    item {
                        Surface(
                            color = if (selectedBatchId == null || selectedBatchId == 0L) Color(0xFFFEF2F2) else Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedBatchId == null || selectedBatchId == 0L) Color(0xFFDC2626) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBatchId = null }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Unassign from Cohort", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF991B1B))
                                    Text("Student will remain active without a designated batch", fontSize = 10.sp, color = Color.Gray)
                                }
                                if (selectedBatchId == null || selectedBatchId == 0L) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFDC2626))
                                }
                            }
                        }
                    }

                    items(allBatches, key = { it.id }) { batch ->
                        val isSelected = selectedBatchId == batch.id
                        val enrolled = allStudents.count { it.assignedBatchId == batch.id && it.id != student.id }
                        val isFull = enrolled >= batch.capacity

                        Surface(
                            color = if (isSelected) Color(0xFFEEF2FF) else Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NavySecondary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBatchId = batch.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(batch.batchName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Text(
                                        text = "${batch.courseName} • Subject: ${batch.subject} • Faculty: ${batch.teacherName}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Schedule: ${batch.scheduleTime} • Capacity: $enrolled / ${batch.capacity} Seats ${if (isFull) "(FULL)" else ""}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isFull) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    )
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NavyPrimary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedBatchId) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("confirm_reassign_batch_button")
            ) {
                Text("Confirm Batch Allocation", color = NavyPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Historical Academic Performance Database Dialog Linked to Student Profile.
 */
@Composable
fun StudentPerformanceHistoryDialog(
    student: StudentEntity,
    batch: BatchEntity?,
    results: List<ExamResultEntity>,
    allExaminations: List<ExaminationEntity>,
    viewModel: FrontCraftViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Historical calculations
    val totalTests = results.size
    val passCount = results.count { it.status == "PASS" }
    val totalMax = results.mapNotNull { r -> allExaminations.find { it.id == r.examId }?.maxMarks }.sum()
    val totalObtained = results.sumOf { it.marksObtained }
    val avgPct = if (totalMax > 0) ((totalObtained / totalMax) * 100).toInt() else 0
    val cumulativeGrade = viewModel.calculateGrade(totalObtained, totalMax)
    val passRate = if (totalTests > 0) ((passCount.toDouble() / totalTests) * 100).toInt() else 0
    val highestScore = results.maxOfOrNull { it.marksObtained } ?: 0.0

    // Match tests with their exam details, sorted descending by date
    val testRecords = results.mapNotNull { res ->
        val exam = allExaminations.find { it.id == res.examId }
        exam?.let { it to res }
    }.sortedByDescending { it.first.examDate }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Historical Performance Profile",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${student.fullName} (${student.studentCode}) • ${batch?.batchName ?: student.courseName}",
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Academic Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CUMULATIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "$avgPct%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }

                    Surface(
                        color = when {
                            cumulativeGrade.startsWith("A") -> Color(0xFFDCFCE7)
                            cumulativeGrade.startsWith("B") -> Color(0xFFE0E7FF)
                            else -> Color(0xFFFEF3C7)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GRADE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = cumulativeGrade,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    cumulativeGrade.startsWith("A") -> Color(0xFF15803D)
                                    cumulativeGrade.startsWith("B") -> Color(0xFF3730A3)
                                    else -> Color(0xFFB45309)
                                }
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PASSED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text(
                                text = "$passCount / $totalTests ($passRate%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOP MARK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                            Text(
                                text = "${highestScore.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar: Average score progress toward 100%
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Academic Proficiency Trend", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Text("$avgPct% of 100%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (avgPct / 100f).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        avgPct >= 80 -> Color(0xFF16A34A)
                                        avgPct >= 60 -> GoldAccent
                                        else -> BurgundyAccent
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HISTORICAL ASSESSMENTS & SCORES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (testRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No exam scores recorded yet", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Schedule tests and record scores in the Exams tab.", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(testRecords, key = { it.second.id }) { (exam, res) ->
                            val scorePct = if (exam.maxMarks > 0) ((res.marksObtained / exam.maxMarks) * 100).toInt() else 0
                            val grade = viewModel.calculateGrade(res.marksObtained, exam.maxMarks)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = exam.examTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "Subject: ${exam.subject} • Date: ${exam.examDate}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = when {
                                                    grade.startsWith("A") -> Color(0xFFDCFCE7)
                                                    grade.startsWith("B") -> Color(0xFFE0E7FF)
                                                    grade.startsWith("C") || grade.startsWith("D") -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFFEE2E2)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Grade $grade",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        grade.startsWith("A") -> Color(0xFF15803D)
                                                        grade.startsWith("B") -> Color(0xFF3730A3)
                                                        grade.startsWith("C") || grade.startsWith("D") -> Color(0xFFB45309)
                                                        else -> Color(0xFFB91C1C)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Surface(
                                                color = if (res.status == "PASS") Color(0xFF16A34A) else Color(0xFFDC2626),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = res.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Marks Scored: ${res.marksObtained.toInt()} / ${exam.maxMarks.toInt()} ($scorePct%)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = NavyPrimary
                                        )

                                        Text(
                                            text = "Passing: ${exam.passingMarks.toInt()}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    if (res.remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Faculty Note: ${res.remarks}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    if (res.submittedBy.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Evaluator: ${res.submittedBy}",
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val transcript = buildString {
                        appendLine("=== STUDENT ACADEMIC TRANSCRIPT ===")
                        appendLine("Student: ${student.fullName} (${student.studentCode})")
                        appendLine("Cohort: ${batch?.batchName ?: student.courseName}")
                        appendLine("Cumulative Score: $avgPct% | Overall Grade: $cumulativeGrade")
                        appendLine("Tests Attempted: $totalTests | Passed: $passCount ($passRate%)")
                        appendLine("Highest Score: ${highestScore.toInt()}")
                        appendLine("-----------------------------------")
                        testRecords.forEachIndexed { i, (exam, res) ->
                            val pct = if (exam.maxMarks > 0) ((res.marksObtained / exam.maxMarks) * 100).toInt() else 0
                            val grade = viewModel.calculateGrade(res.marksObtained, exam.maxMarks)
                            appendLine("${i + 1}. ${exam.examTitle} (${exam.subject}) - ${exam.examDate}")
                            appendLine("   Score: ${res.marksObtained.toInt()}/${exam.maxMarks.toInt()} ($pct%) - Grade $grade [${res.status}]")
                            if (res.remarks.isNotBlank()) appendLine("   Note: ${res.remarks}")
                        }
                    }
                    clipboardManager.setText(AnnotatedString(transcript))
                    Toast.makeText(context, "Student performance report copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("copy_student_transcript_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Transcript", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
