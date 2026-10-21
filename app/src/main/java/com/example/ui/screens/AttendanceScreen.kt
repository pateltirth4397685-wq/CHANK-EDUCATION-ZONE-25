package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecordEntity
import com.example.data.AttendanceSessionEntity
import com.example.data.BatchEntity
import com.example.data.DailyStudentAttendanceEntity
import com.example.ui.FrontCraftViewModel
import com.example.util.CsvExporter
import com.example.ui.components.StatsCard
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allSessions by viewModel.allAttendanceSessions.collectAsState()
    val allRecords by viewModel.allAttendanceRecords.collectAsState()
    val allDailyAttendance by viewModel.allDailyAttendance.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var selectedBatchId by remember { mutableStateOf<Long?>(allBatches.firstOrNull()?.id) }
    var attendanceDate by remember { mutableStateOf(todayDate) }
    var sessionSubject by remember { mutableStateOf("") }
    var batchDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDateDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // Map of studentId -> status (PRESENT, ABSENT, LATE, LEAVE)
    val attendanceStatusMap = remember { mutableStateMapOf<Long, String>() }
    // Map of studentId -> remarks
    val remarksMap = remember { mutableStateMapOf<Long, String>() }

    // Update default session subject whenever batch changes
    LaunchedEffect(selectedBatchId) {
        val b = allBatches.find { it.id == selectedBatchId }
        if (b != null && sessionSubject.isBlank()) {
            sessionSubject = "${b.subject} Daily Session"
        }
    }

    // When batch or date changes, check if attendance was already recorded in database
    LaunchedEffect(selectedBatchId, attendanceDate, allSessions, allRecords, allDailyAttendance) {
        val existingDaily = allDailyAttendance.filter { it.batchId == selectedBatchId && it.attendanceDate == attendanceDate }
        if (existingDaily.isNotEmpty()) {
            existingDaily.forEach { r ->
                attendanceStatusMap[r.studentId] = r.status
                if (r.remarks.isNotBlank()) {
                    remarksMap[r.studentId] = r.remarks
                }
            }
        } else {
            val existingSession = allSessions.find { it.batchId == selectedBatchId && it.attendanceDate == attendanceDate }
            if (existingSession != null) {
                sessionSubject = existingSession.subject
                val recordsForSession = allRecords.filter { it.sessionId == existingSession.id }
                recordsForSession.forEach { r ->
                    attendanceStatusMap[r.studentId] = r.status
                    if (r.remarks.isNotBlank()) {
                        remarksMap[r.studentId] = r.remarks
                    }
                }
            }
        }
    }

    val currentBatch = allBatches.find { it.id == selectedBatchId }
    val currentBatchStudents = allStudents.filter { it.assignedBatchId == selectedBatchId && it.status == "ACTIVE" }

    // Ensure all students in batch have an initial status
    currentBatchStudents.forEach { st ->
        if (!attendanceStatusMap.containsKey(st.id)) {
            attendanceStatusMap[st.id] = "PRESENT"
        }
    }

    // Attendance counts
    val totalCount = currentBatchStudents.size
    val presentCount = currentBatchStudents.count { attendanceStatusMap[it.id] == "PRESENT" }
    val absentCount = currentBatchStudents.count { attendanceStatusMap[it.id] == "ABSENT" }
    val lateCount = currentBatchStudents.count { attendanceStatusMap[it.id] == "LATE" }
    val leaveCount = currentBatchStudents.count { attendanceStatusMap[it.id] == "LEAVE" }
    val attendanceRate = if (totalCount > 0) (presentCount.toFloat() / totalCount) else 0f
    val isSessionAlreadyRecorded = allDailyAttendance.any { it.batchId == selectedBatchId && it.attendanceDate == attendanceDate } ||
            allSessions.any { it.batchId == selectedBatchId && it.attendanceDate == attendanceDate }

    val filteredStudents = currentBatchStudents.filter { st ->
        st.fullName.contains(searchQuery, ignoreCase = true) ||
                st.studentCode.contains(searchQuery, ignoreCase = true) ||
                st.mobile.contains(searchQuery)
    }

    // Helper functions for date stepping
    fun stepDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(attendanceDate) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            attendanceDate = dateFormat.format(cal.time)
        } catch (_: Exception) {
            attendanceDate = todayDate
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title Bar & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Attendance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Mark students present or absent by batch & track daily dates",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        val bName = currentBatch?.batchName?.replace(" ", "_") ?: "batch"
                        val batchRecords = allDailyAttendance.filter { it.batchId == selectedBatchId }
                        val recordsToExport = if (batchRecords.isNotEmpty()) {
                            batchRecords
                        } else {
                            currentBatchStudents.map { st ->
                                DailyStudentAttendanceEntity(
                                    batchId = selectedBatchId ?: 1L,
                                    studentId = st.id,
                                    attendanceDate = attendanceDate,
                                    status = attendanceStatusMap[st.id] ?: "PRESENT",
                                    remarks = remarksMap[st.id] ?: "",
                                    markedBy = currentUser.fullName,
                                    recordedAt = todayDate
                                )
                            }
                        }
                        val csv = CsvExporter.generateAttendanceCsv(recordsToExport, allStudents, allBatches)
                        val fileName = "attendance_${bName}_$attendanceDate.csv"
                        val file = CsvExporter.saveAndShareCsv(context, fileName, csv, "Share Batch Attendance CSV")
                        if (file != null) {
                            Toast.makeText(context, "Exported $fileName", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_attendance_csv_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { showHistoryDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("view_attendance_history")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (selectedBatchId == null) {
                            Toast.makeText(context, "Please select a target batch first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (currentBatchStudents.isEmpty()) {
                            Toast.makeText(context, "No students enrolled in this batch to record", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.recordAttendance(
                            batchId = selectedBatchId!!,
                            attendanceDate = attendanceDate.trim(),
                            subject = if (sessionSubject.isNotBlank()) sessionSubject.trim() else "${currentBatch?.subject ?: "Class"} Session",
                            statusMap = attendanceStatusMap.toMap(),
                            remarksMap = remarksMap.toMap()
                        )

                        Toast.makeText(
                            context,
                            "Saved attendance for $attendanceDate: $presentCount Present, $absentCount Absent",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_attendance_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Roll Call", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Batch Selector & Daily Date Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Batch Dropdown
                ExposedDropdownMenuBox(
                    expanded = batchDropdownExpanded,
                    onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (currentBatch != null) "${currentBatch.batchName} (${currentBatch.subject})" else "Select Batch",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Batch *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("attendance_batch_select")
                    )

                    ExposedDropdownMenu(
                        expanded = batchDropdownExpanded,
                        onDismissRequest = { batchDropdownExpanded = false }
                    ) {
                        allBatches.forEach { b ->
                            val enrolled = allStudents.count { it.assignedBatchId == b.id }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${b.batchName} • ${b.subject}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Faculty: ${b.teacherName} • Enrolled: $enrolled / ${b.capacity}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedBatchId = b.id
                                    sessionSubject = "${b.subject} Session"
                                    batchDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Attendance Date Control with Presets ("add to daily attendance date")
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY ATTENDANCE DATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )

                        if (isSessionAlreadyRecorded) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Recorded in Database", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { stepDate(-1) },
                            modifier = Modifier.size(36.dp).testTag("date_prev_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                        }

                        OutlinedTextField(
                            value = attendanceDate,
                            onValueChange = { attendanceDate = it },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_attendance_date")
                        )

                        IconButton(
                            onClick = { stepDate(1) },
                            modifier = Modifier.size(36.dp).testTag("date_next_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date Preset Chips & "+ New Date"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Today chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { attendanceDate = todayDate },
                            color = if (attendanceDate == todayDate) NavyPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (attendanceDate == todayDate) Color.White else NavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Yesterday chip
                        val yesterdayDate = remember {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -1)
                            dateFormat.format(c.time)
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { attendanceDate = yesterdayDate },
                            color = if (attendanceDate == yesterdayDate) NavyPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Yesterday",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (attendanceDate == yesterdayDate) Color.White else NavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Add to Daily Attendance Date button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showAddDateDialog = true }
                                .testTag("add_daily_date_button"),
                            color = GoldAccent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+ Set Date",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Session Topic Field
                OutlinedTextField(
                    value = sessionSubject,
                    onValueChange = { sessionSubject = it },
                    label = { Text("Session Topic / Curriculum") },
                    placeholder = { Text("e.g. Physics - Kinematics & Vectors") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_session_topic"),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Attendance Metrics & Compliance Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance: $presentCount / $totalCount Present",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Surface(
                        color = if (attendanceRate >= 0.75f) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${(attendanceRate * 100).toInt()}% Present Rate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (attendanceRate >= 0.75f) Color(0xFF15803D) else Color(0xFFB91C1C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { attendanceRate.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (attendanceRate >= 0.75f) Color(0xFF16A34A) else BurgundyAccent,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown Chips & Bulk Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "P: $presentCount", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = "A: $absentCount", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = "L: $lateCount", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = "LV: $leaveCount", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                currentBatchStudents.forEach { st ->
                                    attendanceStatusMap[st.id] = "PRESENT"
                                }
                                Toast.makeText(context, "All marked Present", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("mark_all_present_button")
                        ) {
                            Text("All Present", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }

                        OutlinedButton(
                            onClick = {
                                currentBatchStudents.forEach { st ->
                                    attendanceStatusMap[st.id] = "ABSENT"
                                }
                                Toast.makeText(context, "All marked Absent", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("mark_all_absent_button")
                        ) {
                            Text("All Absent", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Student Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter roster by student name or roll code...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_attendance_student")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Roll Call Student List
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentBatchStudents.isEmpty()) "No students enrolled in this batch" else "No matching students",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val currentStatus = attendanceStatusMap[student.id] ?: "PRESENT"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_student_${student.id}"),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (currentStatus) {
                                                "PRESENT" -> Color(0xFFDCFCE7)
                                                "ABSENT" -> Color(0xFFFEE2E2)
                                                "LATE" -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFDBEAFE)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.fullName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = when (currentStatus) {
                                            "PRESENT" -> Color(0xFF15803D)
                                            "ABSENT" -> Color(0xFFB91C1C)
                                            "LATE" -> Color(0xFFB45309)
                                            else -> Color(0xFF1D4ED8)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                                    Text("${student.studentCode} • 📞 ${student.mobile}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            // Interactive Status Buttons: P (Present), A (Absent), L (Late), LV (Leave)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "P" to "PRESENT",
                                    "A" to "ABSENT",
                                    "L" to "LATE",
                                    "LV" to "LEAVE"
                                ).forEach { (shortCode, statusName) ->
                                    val isSelected = currentStatus == statusName
                                    val bg = when {
                                        isSelected && statusName == "PRESENT" -> Color(0xFF16A34A)
                                        isSelected && statusName == "ABSENT" -> Color(0xFFDC2626)
                                        isSelected && statusName == "LATE" -> Color(0xFFD97706)
                                        isSelected && statusName == "LEAVE" -> Color(0xFF2563EB)
                                        else -> Color(0xFFF1F5F9)
                                    }
                                    val textCol = if (isSelected) Color.White else Color(0xFF475569)

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { attendanceStatusMap[student.id] = statusName }
                                            .testTag("status_${student.id}_$statusName"),
                                        color = bg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = shortCode,
                                            color = textCol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
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

    // Modal: Add / Select Daily Attendance Date Dialog
    if (showAddDateDialog) {
        var tempDate by remember { mutableStateOf(attendanceDate) }

        AlertDialog(
            onDismissRequest = { showAddDateDialog = false },
            title = {
                Text(
                    text = "Select Daily Attendance Date",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter or change the attendance tracking date (YYYY-MM-DD):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = tempDate,
                        onValueChange = { tempDate = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_attendance_date"),
                        singleLine = true
                    )

                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Today" to todayDate, "Yesterday" to remember {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -1)
                            dateFormat.format(c.time)
                        }).forEach { (label, dt) ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { tempDate = dt },
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$label ($dt)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3730A3),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDate.isNotBlank()) {
                            attendanceDate = tempDate.trim()
                            showAddDateDialog = false
                            Toast.makeText(context, "Attendance date switched to $attendanceDate", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Apply Date", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Attendance History Dialog
    if (showHistoryDialog) {
        val historySessions = allSessions.filter { selectedBatchId == null || it.batchId == selectedBatchId }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Past Attendance Sessions", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 18.sp)
                        Text("${currentBatch?.batchName ?: "All Batches"} • ${historySessions.size} recorded sessions", fontSize = 11.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { showHistoryDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                if (historySessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No attendance recorded yet for this batch", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historySessions, key = { it.id }) { session ->
                            val batch = allBatches.find { it.id == session.batchId }
                            val records = allRecords.filter { it.sessionId == session.id }
                            val pCount = records.count { it.status == "PRESENT" }
                            val total = records.size

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        attendanceDate = session.attendanceDate
                                        sessionSubject = session.subject
                                        showHistoryDialog = false
                                        Toast.makeText(context, "Loaded roll call for ${session.attendanceDate}", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "📅 ${session.attendanceDate}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "${batch?.batchName ?: "Batch"} • ${session.subject}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Recorded by: ${session.createdBy}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "$pCount / $total Present",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showHistoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
